package org.unibl.etf.mdp.dobavljacserver.app;

import org.unibl.etf.mdp.dobavljacserver.model.Book;
import org.unibl.etf.mdp.dobavljacserver.service.BookService;

public class App {

	public static void main(String[] args) {
		Book book = BookService.getBookFromUrl("https://www.gutenberg.org/cache/epub/27761/pg27761.txt");
		System.out.println(book);
		String username = "test";
		BookService.saveBookToFile(book, username);
	}

}
