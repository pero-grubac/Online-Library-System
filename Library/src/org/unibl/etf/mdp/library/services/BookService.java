package org.unibl.etf.mdp.library.services;

import java.util.List;

import org.unibl.etf.mdp.library.interfaces.BookObserver;
import org.unibl.etf.mdp.library.model.Book;

public class BookService implements BookObserver {

	@Override
	public void onBooksArrived(List<Book> books) {
		System.out.println("Processing received books: " + books);
		
	}

}
