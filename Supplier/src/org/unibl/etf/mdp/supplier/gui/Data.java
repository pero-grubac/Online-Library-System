package org.unibl.etf.mdp.supplier.gui;

import java.util.List;

import org.unibl.etf.mdp.model.BookDto;

public class Data {
	private static Data instance = null;
	private List<BookDto> books;

	private Data(List<BookDto> books) {
		this.books = books;
	}

	public static synchronized Data getInstance(List<BookDto> books) {
		if (instance == null) {
			instance = new Data(books);
		}
		return instance;
	}

	public List<BookDto> getBooks() {
		return books;
	}

	public void setBooks(List<BookDto> books) {
		this.books = books;
	}

	

}
