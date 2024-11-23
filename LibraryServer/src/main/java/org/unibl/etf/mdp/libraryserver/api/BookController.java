package org.unibl.etf.mdp.libraryserver.api;

import java.net.URLDecoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.unibl.etf.mdp.libraryserver.logger.FileLogger;
import org.unibl.etf.mdp.libraryserver.service.BookService;
import org.unibl.etf.mdp.libraryserver.service.EmailService;
import org.unibl.etf.mdp.libraryserver.service.UserService;
import org.unibl.etf.mdp.libraryserver.service.ZipService;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.UserDto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
@Path("/books")
public class BookController {
	private static final Logger logger = FileLogger.getLogger(BookController.class.getName());

	private final BookService service;
	private final ZipService zipService;
	private final EmailService emailService;
	private final UserService userService;

	public BookController() {
		service = new BookService();
		zipService = new ZipService();
		emailService = new EmailService();
		userService = new UserService();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllBooks() {
		try {
			List<BookDto> books = service.getAll();
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
			return Response.ok(gson.toJson(books)).build();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while fetching books: " + e.getMessage(), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error fetching books").build();
		}
	}

	@GET
	@Path("/{key}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBookByKey(@PathParam("key") String key) {
		try {
			Book book = service.getByKey(key);

			if (book == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("Book not found").build();
			}

			return Response.ok(book).build();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while fetching book: " + e.getMessage(), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error fetching book").build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response add(String bookJson) {
		try {
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
			Book book = gson.fromJson(bookJson, Book.class);
			if (book == null || book.getTitle() == null || book.getTitle().isBlank() || book.getAuthor() == null
					|| book.getAuthor().isBlank() || book.getLanguage() == null || book.getLanguage().isBlank()) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Invalid book data: Missing required fields")
						.build();
			}
			BookDto bookDto = service.add(book);

			if (bookDto == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Invalid book data").build();
			}
			System.out.println(bookDto.toString());

			return Response.status(Response.Status.CREATED).entity(bookDto).build();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while adding book: " + e.getMessage(), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error adding book").build();
		}
	}

	@DELETE
	@Path("/{key}")
	public Response deleteBook(@PathParam("key") String key) {
		try {
			if (key == null || key.isBlank()) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Invalid key").build();
			}
			String decodedKey = URLDecoder.decode(key, "UTF-8");

			boolean deleted = service.delete(decodedKey);

			if (!deleted) {
				return Response.status(Response.Status.NOT_FOUND).entity("Book not found").build();
			}

			return Response.status(Response.Status.NO_CONTENT).build();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while deleting book: " + e.getMessage(), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error deleting book").build();
		}
	}

	@PUT
	@Path("/email/{username}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendEmail(@PathParam("username") String username, String booksJson) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
		Type listType = new TypeToken<List<BookDto>>() {
		}.getType();
		List<BookDto> books;

		try {
			// Parsiranje liste knjiga
			books = gson.fromJson(booksJson, listType);
			if (books == null || books.isEmpty()) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Book list cannot be empty.").build();
			}
		} catch (JsonSyntaxException e) {
			logger.log(Level.SEVERE, "Invalid JSON format: " + e.getMessage(), e);
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid JSON format.").build();
		}

		// Dohvatanje korisnika
		UserDto user = userService.getByUsername(username);
		if (user == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("User not found.").build();
		}

		try {
			// Kreiranje ZIP fajla i slanje email-a
			String pathToZip = zipService.zipBooks(books);
			if (pathToZip == null) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to create ZIP file.")
						.build();
			}

			emailService.sendEmail(pathToZip, user.getEmail(), books);
			return Response.ok("Email sent successfully.").build();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "An error occurred while sending the email: " + e.getMessage(), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("An error occurred while sending the email: " + e.getMessage()).build();
		}
	}

}
