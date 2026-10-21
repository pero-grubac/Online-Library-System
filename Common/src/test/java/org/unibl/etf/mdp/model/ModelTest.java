package org.unibl.etf.mdp.model;

import org.junit.jupiter.api.Test;
import org.unibl.etf.mdp.common.Json;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelTest {

    private static BookDto book(String title) {
        BookDto book = new BookDto();
        book.setTitle(title);
        book.setAuthor("Author");
        return book;
    }

    @Test
    void newInvoiceIsEmptyAndDatedToday() {
        Invoice invoice = new Invoice();

        assertThat(invoice.getItems()).isEmpty();
        assertThat(invoice.getTotalPrice()).isZero();
        assertThat(invoice.getDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void invoiceTotalIsQuantityTimesUnitPrice() {
        Invoice invoice = new Invoice();
        invoice.addItem(book("Emma"), 3, 10);
        invoice.addItem(book("Dracula"), 1, 12);

        assertThat(invoice.getTotalPrice()).isEqualTo(42);
        assertThat(invoice.getItems()).extracting(InvoiceItem::getLineTotal).containsExactly(30, 12);
        assertThat(invoice.toString()).contains("Emma: 3 x 10 = 30").contains("Total Price: 42");
    }

    @Test
    void orderNeedsAtLeastOneCopy() {
        assertThat(new OrderItem(book("Emma"), 2).getQuantity()).isEqualTo(2);
        assertThatThrownBy(() -> new OrderItem(book("Emma"), 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chatMessagesAreIdentifiedByIdNotContent() {
        LocalDateTime now = LocalDateTime.now();
        ChatMessage first = new ChatMessage("ana", "hi", now);
        ChatMessage second = new ChatMessage("ana", "hi", now);

        assertThat(first).isNotEqualTo(second);
        assertThat(Json.fromJson(Json.toJson(first), ChatMessage.class)).isEqualTo(first);
    }

    @Test
    void answeringAProposalKeepsItsIdentity() {
        Proposal open = new Proposal("ana", "Ulysses", "James Joyce", "For the reading club");
        Proposal answer = open.answered(true, "Arriving next week", "Library");

        assertThat(open.isAnswered()).isFalse();
        assertThat(open.getStatusText()).isEqualTo("Waiting for the library");
        assertThat(answer).isEqualTo(open);
        assertThat(answer.getAccepted()).isTrue();
        assertThat(answer.getStatusText()).isEqualTo("Will be acquired");
        assertThat(answer.getTitle()).isEqualTo("Ulysses");
        assertThat(open.answered(false, "", "Library").getStatusText()).isEqualTo("Will not be acquired");
    }

    @Test
    void reservationSurvivesJson() {
        Reservation reservation = new Reservation(7, "ana", "key", "Emma", "Jane Austen",
                LocalDateTime.of(2024, 3, 15, 10, 30), ReservationStatus.PENDING);

        Reservation copy = Json.fromJson(Json.toJson(reservation), Reservation.class);
        assertThat(copy).isEqualTo(reservation);
        assertThat(copy.getCreatedAt()).isEqualTo(reservation.getCreatedAt());
        assertThat(copy.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }
}
