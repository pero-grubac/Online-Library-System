package org.unibl.etf.mdp.libraryserver.service;

import org.unibl.etf.mdp.libraryserver.repository.BookRepository;
import org.unibl.etf.mdp.libraryserver.repository.ReservationRepository;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.Reservation;
import org.unibl.etf.mdp.model.ReservationStatus;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** Members reserve books; the library confirms or rejects each reservation once. */
public class ReservationService {
    private final ReservationRepository reservations;
    private final BookRepository books;
    private final Clock clock;

    public ReservationService() {
        this(ReservationRepository.getInstance(), BookRepository.getInstance(), Clock.systemDefaultZone());
    }

    public ReservationService(ReservationRepository reservations, BookRepository books, Clock clock) {
        this.reservations = reservations;
        this.books = books;
        this.clock = clock;
    }

    public static class ReservationException extends Exception {
        private final int status;

        public ReservationException(int status, String message) {
            super(message);
            this.status = status;
        }

        /** The HTTP status that describes the problem. */
        public int getStatus() {
            return status;
        }
    }

    public Reservation reserve(String username, String bookKey) throws ReservationException {
        if (bookKey == null || bookKey.isBlank()) {
            throw new ReservationException(400, "A book key is required");
        }
        Book book = books.getByKey(bookKey);
        if (book == null) {
            throw new ReservationException(404, "Book not found");
        }
        boolean alreadyWaiting = forMember(username).stream()
                .anyMatch(r -> r.getBookKey().equals(bookKey) && r.getStatus() == ReservationStatus.PENDING);
        if (alreadyWaiting) {
            throw new ReservationException(409, "You already have a pending reservation for this book");
        }
        return reservations.create(new Reservation(0, username, bookKey, book.getTitle(), book.getAuthor(),
                LocalDateTime.now(clock), ReservationStatus.PENDING));
    }

    public List<Reservation> all() {
        return reservations.findAll();
    }

    public List<Reservation> forMember(String username) {
        return reservations.findAll().stream().filter(r -> r.getUsername().equals(username))
                .collect(Collectors.toList());
    }

    public Reservation decide(long id, ReservationStatus decision) throws ReservationException {
        if (decision == null || decision == ReservationStatus.PENDING) {
            throw new ReservationException(400, "Status must be APPROVED or REJECTED");
        }
        Reservation reservation = reservations.findById(id);
        if (reservation == null) {
            throw new ReservationException(404, "Reservation not found");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ReservationException(409, "Reservation is already " + reservation.getStatus());
        }
        reservations.updateStatus(id, decision);
        reservation.setStatus(decision);
        return reservation;
    }
}
