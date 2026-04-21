package org.unibl.etf.mdp.library.event;

import org.unibl.etf.mdp.model.Book;

import java.util.List;

public class BookArrivalEvent extends Event {
    private final List<Book> books;

    public BookArrivalEvent(List<Book> books) {
        this.books = books;
    }

    public List<Book> getBooks() {
        return books;
    }
}
