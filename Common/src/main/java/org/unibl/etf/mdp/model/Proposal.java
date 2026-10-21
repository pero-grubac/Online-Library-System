package org.unibl.etf.mdp.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * A member's suggestion that the library acquire a book. It travels by multicast to the librarians and
 * all members; the librarians' answer travels the same way as the same proposal with the decision filled in.
 */
public class Proposal {
    private String id;
    private String username;
    private String title;
    private String author;
    private String note;
    private LocalDateTime createdAt;
    // Null until a librarian answers
    private Boolean accepted;
    private String answer;
    private String answeredBy;

    public Proposal() {
    }

    public Proposal(String username, String title, String author, String note) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.title = title;
        this.author = author;
        this.note = note;
        this.createdAt = LocalDateTime.now();
    }

    /** A copy carrying the librarian's decision. */
    public Proposal answered(boolean accepted, String answer, String librarian) {
        Proposal copy = new Proposal();
        copy.id = id;
        copy.username = username;
        copy.title = title;
        copy.author = author;
        copy.note = note;
        copy.createdAt = createdAt;
        copy.accepted = accepted;
        copy.answer = answer;
        copy.answeredBy = librarian;
        return copy;
    }

    public boolean isAnswered() {
        return accepted != null;
    }

    public String getStatusText() {
        if (accepted == null) {
            return "Waiting for the library";
        }
        return accepted ? "Will be acquired" : "Will not be acquired";
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public String getAnswer() {
        return answer;
    }

    public String getAnsweredBy() {
        return answeredBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Proposal))
            return false;
        return Objects.equals(id, ((Proposal) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return title + " (" + author + ") proposed by " + username + ": " + getStatusText();
    }
}
