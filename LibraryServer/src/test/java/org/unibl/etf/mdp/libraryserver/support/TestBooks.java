package org.unibl.etf.mdp.libraryserver.support;

import org.unibl.etf.mdp.model.Book;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class TestBooks {
    public static final String START_MARKER = "*** START OF THE PROJECT GUTENBERG EBOOK";

    private TestBooks() {
    }

    public static Date date(String yyyyMmDd) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(yyyyMmDd);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** A Gutenberg-like book: a header, the start marker and ten numbered lines of text. */
    public static Book book(String title, String author) {
        Book book = new Book(title, author, "English", date("1813-01-28"), 42);
        StringBuilder content = new StringBuilder("Title: " + title + "\nAuthor: " + author + "\n");
        content.append(START_MARKER).append(" ").append(title.toUpperCase()).append(" ***\n");
        for (int i = 1; i <= 10; i++) {
            content.append("line ").append(i).append("\n");
        }
        book.setContent(content.toString());
        return book;
    }
}
