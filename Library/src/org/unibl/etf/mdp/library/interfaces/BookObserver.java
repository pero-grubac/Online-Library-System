package org.unibl.etf.mdp.library.interfaces;

import java.util.List;

import org.unibl.etf.mdp.library.model.Book;

public interface BookObserver {
	void onBooksArrived(List<Book> books);
}
