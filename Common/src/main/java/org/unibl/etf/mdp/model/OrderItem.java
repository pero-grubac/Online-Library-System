package org.unibl.etf.mdp.model;

import java.util.Objects;

/** One line of a library order: a book from the supplier's offer and how many copies. */
public class OrderItem {
    private BookDto book;
    private int quantity;

    public OrderItem() {
    }

    public OrderItem(BookDto book, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        this.book = book;
        this.quantity = quantity;
    }

    public BookDto getBook() {
        return book;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OrderItem))
            return false;
        OrderItem other = (OrderItem) o;
        return quantity == other.quantity && Objects.equals(book, other.book);
    }

    @Override
    public int hashCode() {
        return Objects.hash(book, quantity);
    }

    @Override
    public String toString() {
        return book + " x " + quantity;
    }
}
