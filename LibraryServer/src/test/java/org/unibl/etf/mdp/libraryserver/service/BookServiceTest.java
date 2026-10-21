package org.unibl.etf.mdp.libraryserver.service;

import org.junit.jupiter.api.Test;
import org.unibl.etf.mdp.libraryserver.repository.BookRepository;
import org.unibl.etf.mdp.libraryserver.support.TestBooks;
import org.unibl.etf.mdp.model.Book;
import redis.clients.jedis.JedisPool;

import static org.assertj.core.api.Assertions.assertThat;

/** Validation runs before Redis is touched, so no server is needed here. */
class BookServiceTest {
    // The pool connects lazily; nothing listens on port 1
    private final BookService service = new BookService(new BookRepository(new JedisPool("localhost", 1)));

    @Test
    void rejectsBooksWithoutRequiredFields() {
        Book noTitle = TestBooks.book("x", "Author");
        noTitle.setTitle(" ");
        Book noContent = TestBooks.book("Title", "Author");
        noContent.setContent("");

        assertThat(service.add(null)).isNull();
        assertThat(service.add(noTitle)).isNull();
        assertThat(service.add(noContent)).isNull();
    }
}
