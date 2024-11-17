package org.unibl.etf.mdp.library.services;

import java.util.ArrayList;
import java.util.List;

import org.unibl.etf.mdp.library.event.BookArrivalEvent;
import org.unibl.etf.mdp.library.event.Event;
import org.unibl.etf.mdp.library.interfaces.Observer;
import org.unibl.etf.mdp.library.model.Book;
import org.unibl.etf.mdp.library.observer.BookObserver;

public class BookService {

	private static BookService instance;
	private final List<BookObserver> observers = new ArrayList<>();

	private BookService() {
	}

	public static synchronized BookService getInstance() {
		if (instance == null) {
			instance = new BookService();
		}
		return instance;
	}

	public void addObserver(BookObserver observer) {
		observers.add(observer);
	}

	public void removeObserver(BookObserver observer) {
		observers.remove(observer);
	}

	public void notifyBookArrival(List<Book> books) {
		BookArrivalEvent event = new BookArrivalEvent(books);
		notifyObservers(event);
	}

	private void notifyObservers(Event event) {
		for (BookObserver observer : observers) {
			observer.onEvent(event);
		}
	}
}
