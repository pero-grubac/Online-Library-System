package org.unibl.etf.mdp.model;

import org.junit.jupiter.api.Test;
import org.unibl.etf.mdp.libraryserver.support.TestBooks;

import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BookTest {

    @Test
    void keyIsLowercaseAndSafeForRedis() {
        Book book = new Book("Pride & Prejudice", "Jane Austen", "English", TestBooks.date("1813-01-28"), 10);

        assertThat(book.getKey()).isEqualTo("jane_austen:pride_prejudice:english:28011813");
    }

    @Test
    void bookAndItsDtoShareTheSameKey() {
        Book book = TestBooks.book("Dracula", "Bram Stoker");

        assertThat(new BookDto(book).getKey()).isEqualTo(book.getKey());
    }

    @Test
    void hashMapRoundTripKeepsEveryField() {
        Book book = TestBooks.book("Dracula", "Bram Stoker");
        book.setCoverImageBase64(Base64.getEncoder().encodeToString(new byte[]{1, 2, 3}));

        Book restored = Book.fromHashMap(book.toHashMap());

        assertThat(restored).isEqualTo(book);
        assertThat(restored.getContent()).isEqualTo(book.getContent());
        assertThat(restored.getPrice()).isEqualTo(42);
        assertThat(restored.getCoverImageBytes()).containsExactly(1, 2, 3);
    }

    @Test
    void missingFieldsGetSafeDefaults() {
        Book restored = Book.fromHashMap(Map.of());

        assertThat(restored.getTitle()).isEmpty();
        assertThat(restored.getPrice()).isZero();
        assertThat(restored.getReleaseDate()).isNull();
        assertThat(restored.getCoverImageBytes()).isNull();
        assertThat(restored.getFormatedDate()).isEqualTo("N/A");
    }

    @Test
    void toStringCanBeUsedAsAFileName() {
        Book book = new Book("Who? What: Where/When", "A*B", "English", TestBooks.date("2000-01-01"), 1);

        assertThat(book.toString()).doesNotContainPattern("[:\\\\/*?|<>]");
    }
}
