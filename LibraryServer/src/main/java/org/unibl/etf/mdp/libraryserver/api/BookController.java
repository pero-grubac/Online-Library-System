package org.unibl.etf.mdp.libraryserver.api;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.unibl.etf.mdp.libraryserver.logger.FileLogger;
import org.unibl.etf.mdp.libraryserver.service.BookService;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;

@Path("/books")
public class BookController {
	private static final Logger logger = FileLogger.getLogger(BookController.class.getName());

	private final BookService service;

	public BookController() {
		service = new BookService();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllBooks() {
		try {
			List<BookDto> books = service.getAll();
			return Response.ok(books).build();
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
	public Response add(Book book) {
	    try {
	        if (book == null || book.getTitle() == null || book.getTitle().isBlank() || 
	            book.getAuthor() == null || book.getAuthor().isBlank() || 
	            book.getLanguage() == null || book.getLanguage().isBlank()) {
	            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid book data: Missing required fields").build();
	        }

	        BookDto bookDto = service.add(book);

	        if (bookDto == null) {
	            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid book data").build();
	        }

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
	        boolean deleted = service.delete(key);

	        if (!deleted) {
	            return Response.status(Response.Status.NOT_FOUND).entity("Book not found").build();
	        }

	        return Response.status(Response.Status.NO_CONTENT).build();
	    } catch (Exception e) {
	        logger.log(Level.SEVERE, "Error while deleting book: " + e.getMessage(), e);
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error deleting book").build();
	    }
	}

}
