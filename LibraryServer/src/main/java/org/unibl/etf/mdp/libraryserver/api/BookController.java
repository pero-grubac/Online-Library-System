package org.unibl.etf.mdp.libraryserver.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.unibl.etf.mdp.libraryserver.logger.FileLogger;
import org.unibl.etf.mdp.libraryserver.security.Roles;
import org.unibl.etf.mdp.libraryserver.service.BookService;
import org.unibl.etf.mdp.libraryserver.service.BookService.BookConflictException;
import org.unibl.etf.mdp.libraryserver.service.EmailService;
import org.unibl.etf.mdp.libraryserver.service.UserService;
import org.unibl.etf.mdp.libraryserver.service.ZipService;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.UserDto;

import javax.annotation.security.RolesAllowed;
import javax.mail.MessagingException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/books")
public class BookController {
    private static final Logger logger = FileLogger.getLogger(BookController.class.getName());
    // One date format for every book on this API, in both directions
    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    private final BookService service;
    private final ZipService zipService;
    private final EmailService emailService;
    private final UserService userService;

    public BookController() {
        service = new BookService();
        zipService = new ZipService(service);
        emailService = new EmailService();
        userService = new UserService();
    }

    private static Response json(Response.Status status, Object body) {
        return Response.status(status).entity(gson.toJson(body)).type(MediaType.APPLICATION_JSON).build();
    }

    private static Response text(Response.Status status, String message) {
        return Response.status(status).entity(message).type(MediaType.TEXT_PLAIN).build();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @GET
    @RolesAllowed({Roles.MEMBER, Roles.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllBooks() {
        try {
            return json(Response.Status.OK, service.getAll());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while fetching books: " + e.getMessage(), e);
            return text(Response.Status.INTERNAL_SERVER_ERROR, "Error fetching books");
        }
    }

    @GET
    @RolesAllowed({Roles.MEMBER, Roles.ADMIN})
    @Path("/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBookByKey(@PathParam("key") String key) {
        try {
            Book book = service.getByKey(key);
            if (book == null) {
                return text(Response.Status.NOT_FOUND, "Book not found");
            }
            return json(Response.Status.OK, book);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while fetching book: " + e.getMessage(), e);
            return text(Response.Status.INTERNAL_SERVER_ERROR, "Error fetching book");
        }
    }

    @POST
    @RolesAllowed(Roles.ADMIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(String bookJson) {
        try {
            Book book = gson.fromJson(bookJson, Book.class);
            if (book == null || isBlank(book.getTitle()) || isBlank(book.getAuthor()) || isBlank(book.getLanguage())) {
                return text(Response.Status.BAD_REQUEST, "Invalid book data: Missing required fields");
            }
            BookDto bookDto = service.add(book);
            if (bookDto == null) {
                return text(Response.Status.BAD_REQUEST, "Invalid book data: the text of the book is required");
            }
            return json(Response.Status.CREATED, bookDto);
        } catch (JsonParseException e) {
            return text(Response.Status.BAD_REQUEST, "Invalid JSON format.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while adding book: " + e.getMessage(), e);
            return text(Response.Status.INTERNAL_SERVER_ERROR, "Error adding book");
        }
    }

    /** Edits a book; text and cover may be left out to keep the stored ones. */
    @PUT
    @RolesAllowed(Roles.ADMIN)
    @Path("/{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("key") String key, String bookJson) {
        try {
            BookDto updated = service.update(key, gson.fromJson(bookJson, Book.class));
            if (updated == null) {
                return text(Response.Status.NOT_FOUND, "Book not found");
            }
            return json(Response.Status.OK, updated);
        } catch (JsonParseException e) {
            return text(Response.Status.BAD_REQUEST, "Invalid JSON format.");
        } catch (IllegalArgumentException e) {
            return text(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (BookConflictException e) {
            return text(Response.Status.CONFLICT, e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while updating book: " + e.getMessage(), e);
            return text(Response.Status.INTERNAL_SERVER_ERROR, "Error updating book");
        }
    }

    @DELETE
    @RolesAllowed(Roles.ADMIN)
    @Path("/{key}")
    public Response deleteBook(@PathParam("key") String key) {
        try {
            if (isBlank(key)) {
                return text(Response.Status.BAD_REQUEST, "Invalid key");
            }
            // JAX-RS has already decoded the path parameter
            if (!service.delete(key)) {
                return text(Response.Status.NOT_FOUND, "Book not found");
            }
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while deleting book: " + e.getMessage(), e);
            return text(Response.Status.INTERNAL_SERVER_ERROR, "Error deleting book");
        }
    }

    @PUT
    @RolesAllowed({Roles.MEMBER, Roles.ADMIN})
    @Path("/email/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendEmail(@PathParam("username") String username, String booksJson,
                              @Context SecurityContext caller) {
        // Members may only send books to their own address
        if (!caller.isUserInRole(Roles.ADMIN) && !caller.getUserPrincipal().getName().equals(username)) {
            return text(Response.Status.FORBIDDEN, "Not allowed");
        }
        Type listType = new TypeToken<List<BookDto>>() {
        }.getType();
        List<BookDto> books;
        try {
            books = gson.fromJson(booksJson, listType);
            if (books == null || books.isEmpty()) {
                return text(Response.Status.BAD_REQUEST, "Book list cannot be empty.");
            }
        } catch (JsonParseException e) {
            logger.log(Level.WARNING, "Invalid JSON format: " + e.getMessage());
            return text(Response.Status.BAD_REQUEST, "Invalid JSON format.");
        }

        UserDto user = userService.getByUsername(username);
        if (user == null) {
            return text(Response.Status.NOT_FOUND, "User not found.");
        }
        if (!emailService.isConfigured()) {
            return text(Response.Status.SERVICE_UNAVAILABLE, "E-mail is not configured on the server.");
        }

        java.nio.file.Path zip = null;
        try {
            zip = zipService.zipBooks(books);
            if (zip == null) {
                return text(Response.Status.NOT_FOUND, "None of the selected books exists any more.");
            }
            emailService.sendEmail(zip, user.getEmail(), books);
            return text(Response.Status.OK, "Books sent to " + user.getEmail() + ".");
        } catch (MessagingException e) {
            logger.log(Level.SEVERE, "Could not send the e-mail to " + user.getEmail(), e);
            return text(Response.Status.BAD_GATEWAY, "The e-mail could not be sent: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while preparing the e-mail: " + e.getMessage(), e);
            return text(Response.Status.INTERNAL_SERVER_ERROR, "Error while preparing the e-mail.");
        } finally {
            if (zip != null) {
                try {
                    Files.deleteIfExists(zip);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Could not delete " + zip, e);
                }
            }
        }
    }
}
