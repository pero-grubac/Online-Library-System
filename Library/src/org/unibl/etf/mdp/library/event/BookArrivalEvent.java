package org.unibl.etf.mdp.library.event;

import java.util.List;

import org.unibl.etf.mdp.library.model.Book;

public class BookArrivalEvent extends Event {
	private final List<Book> books;

	public BookArrivalEvent(List<Book> books) {
		this.books = books;
	}

	public List<Book> getBooks() {
		return books;
	}
}
