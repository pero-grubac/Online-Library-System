package org.unibl.etf.mdp.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** A member's request to reserve a book; the library confirms or rejects it. */
public class Reservation {
    private long id;
    private String username;
    private String bookKey;
    private String bookTitle;
    private String bookAuthor;
    private LocalDateTime createdAt;
    private ReservationStatus status;

    public Reservation() {
    }

    public Reservation(long id, String username, String bookKey, String bookTitle, String bookAuthor,
                       LocalDateTime createdAt, ReservationStatus status) {
        this.id = id;
        this.username = username;
        this.bookKey = bookKey;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.createdAt = createdAt;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBookKey() {
        return bookKey;
    }

    public void setBookKey(String bookKey) {
        this.bookKey = bookKey;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Reservation))
            return false;
        return id == ((Reservation) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "#" + id + " " + username + " -> " + bookTitle + " [" + status + "]";
    }
}
