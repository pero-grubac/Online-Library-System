package org.unibl.etf.mdp.common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Invoice;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SerialFilterTest {

    @BeforeAll
    static void install() {
        SerialFilter.install();
        SerialFilter.install();
    }

    private static Object roundTrip(Object value) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(value);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return in.readObject();
        }
    }

    @Test
    void invoicesStillPass() throws Exception {
        Invoice invoice = new Invoice();
        invoice.setDate(LocalDate.of(2024, 3, 15));
        BookDto book = new BookDto();
        book.setTitle("Emma");
        book.setPrice(30);
        invoice.addItem(book, 2, 15);

        assertThat(roundTrip(invoice)).isEqualTo(invoice);
    }

    @Test
    void classesOutsideTheAllowListAreRejected() {
        // java.desktop is not on the list; a gadget chain would be rejected the same way
        assertThatThrownBy(() -> roundTrip(new Point(1, 2))).isInstanceOf(InvalidClassException.class);
    }
}
