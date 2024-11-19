package org.unibl.etf.mdp.library.observer;

import java.util.List;

import org.unibl.etf.mdp.library.event.BookArrivalEvent;
import org.unibl.etf.mdp.library.event.Event;
import org.unibl.etf.mdp.library.interfaces.Observer;
import org.unibl.etf.mdp.model.Book;

public class BookObserver implements Observer {
	@Override
	public void onEvent(Event event) {
		if (event instanceof BookArrivalEvent) {
			List<Book> books = ((BookArrivalEvent) event).getBooks();
			handleBooksArrival(books);
		} else {
			System.err.println("Unexpected event type: " + event.getClass().getName());
		}
	}

	private void handleBooksArrival(List<Book> books) {
		System.out.println("Books arrived: " + books);
		// TODO call libraryserver add book
	}
}