package org.unibl.etf.mdp.libraryserver.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.unibl.etf.mdp.libraryserver.service.BookService;
import org.unibl.etf.mdp.libraryserver.service.ZipService;
import org.unibl.etf.mdp.libraryserver.support.TestBooks;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Runs against a real Redis in Docker; skipped when Docker is not available. */
@Testcontainers(disabledWithoutDocker = true)
class BookRepositoryRedisTest {

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    static JedisPool pool;
    BookRepository repository;
    BookService service;

    @BeforeAll
    static void connect() {
        pool = new JedisPool(REDIS.getHost(), REDIS.getMappedPort(6379));
    }

    @AfterAll
    static void disconnect() {
        pool.close();
    }

    @BeforeEach
    void emptyDatabase() {
        try (Jedis jedis = pool.getResource()) {
            jedis.flushAll();
        }
        repository = new BookRepository(pool);
        service = new BookService(repository);
    }

    @Test
    void storesAndReadsABookAsAHash() {
        Book book = TestBooks.book("Dracula", "Bram Stoker");
        repository.create(book);

        try (Jedis jedis = pool.getResource()) {
            assertThat(jedis.hget("test-book:" + book.getKey(), "title")).isEqualTo("Dracula");
        }
        assertThat(repository.getByKey(book.getKey())).isEqualTo(book);
        assertThat(repository.getByKey("missing")).isNull();
    }

    @Test
    void listsAndDeletesBooks() {
        Book dracula = TestBooks.book("Dracula", "Bram Stoker");
        repository.create(dracula);
        repository.create(TestBooks.book("Emma", "Jane Austen"));

        assertThat(repository.getAll()).hasSize(2);
        assertThat(repository.deleteByKey(dracula.getKey())).isTrue();
        assertThat(repository.deleteByKey(dracula.getKey())).isFalse();
        assertThat(repository.getAll()).extracting(Book::getTitle).containsExactly("Emma");
    }

    @Test
    void listsEveryBookAcrossManyScanPagesAndIgnoresOtherKeys() {
        for (int i = 0; i < 450; i++) {
            Book book = TestBooks.book("Title " + i, "Author");
            repository.create(book);
        }
        try (Jedis jedis = pool.getResource()) {
            jedis.set("other:key", "not a book");
        }

        assertThat(repository.getAll()).hasSize(450).extracting(Book::getTitle).doesNotHaveDuplicates();
    }

    @Test
    void previewStartsAfterTheGutenbergMarker() {
        // PREVIEW_LINES is 3 in the test configuration
        BookDto dto = service.add(TestBooks.book("Dracula", "Bram Stoker"));

        assertThat(dto.getPreview()).isEqualTo("line 1\nline 2\nline 3");
        assertThat(service.getAll()).singleElement().extracting(BookDto::getPreview).isEqualTo(dto.getPreview());
    }

    @Test
    void zipContainsTheFullTextOfTheRequestedBooks() throws Exception {
        Book dracula = TestBooks.book("Dracula", "Bram Stoker");
        Book emma = TestBooks.book("Emma", "Jane Austen");
        repository.create(dracula);
        repository.create(emma);
        BookDto notStored = new BookDto(TestBooks.book("Not stored", "Nobody"));

        Path zipPath = new ZipService(service).zipBooks(List.of(new BookDto(dracula), new BookDto(emma), notStored));

        try (ZipFile zip = new ZipFile(zipPath.toFile())) {
            assertThat(zip.stream().map(ZipEntry::getName)).containsExactlyInAnyOrder("Dracula.txt", "Emma.txt");
            String text = new String(zip.getInputStream(zip.getEntry("Dracula.txt")).readAllBytes(),
                    StandardCharsets.UTF_8);
            assertThat(text).isEqualTo(dracula.getContent());
        }
        Files.deleteIfExists(zipPath);
    }

    @Test
    void everyRequestGetsItsOwnZipAndTextStaysUtf8() throws Exception {
        Book book = TestBooks.book("Čuvari", "Ivo Andrić");
        book.setContent("Ćirilica i latinica: šđčćž ЉЊЏ\n");
        repository.create(book);
        ZipService zips = new ZipService(service);

        Path first = zips.zipBooks(List.of(new BookDto(book)));
        Path second = zips.zipBooks(List.of(new BookDto(book)));

        assertThat(first).isNotEqualTo(second);
        try (ZipFile zip = new ZipFile(first.toFile())) {
            ZipEntry entry = zip.entries().nextElement();
            assertThat(new String(zip.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8))
                    .isEqualTo(book.getContent());
        }
        Files.deleteIfExists(first);
        Files.deleteIfExists(second);
        assertThat(zips.zipBooks(List.of(new BookDto(TestBooks.book("Gone", "Nobody"))))).isNull();
    }

    @Test
    void bookWithoutReleaseDateCanBeStored() {
        Book book = TestBooks.book("Undated", "Someone");
        book.setReleaseDate(null);

        repository.create(book);

        assertThat(repository.getByKey(book.getKey())).isNotNull();
    }

    @Test
    void editingKeepsTextAndCoverWhenTheyAreLeftOut() throws Exception {
        Book book = TestBooks.book("Dracula", "Bram Stoker");
        book.setCoverImageBase64("Y292ZXI=");
        repository.create(book);
        Book changes = new Book("Dracula", "Bram Stoker", "English", book.getReleaseDate(), 15);

        BookDto updated = service.update(book.getKey(), changes);

        assertThat(updated.getPrice()).isEqualTo(15);
        Book stored = repository.getByKey(book.getKey());
        assertThat(stored.getContent()).isEqualTo(book.getContent());
        assertThat(stored.getCoverImageBase64()).isEqualTo("Y292ZXI=");
    }

    @Test
    void renamingABookMovesItToTheNewKey() throws Exception {
        Book book = TestBooks.book("Dracla", "Bram Stoker");
        repository.create(book);
        Book changes = new Book("Dracula", "Bram Stoker", "English", book.getReleaseDate(), 0);

        service.update(book.getKey(), changes);

        assertThat(repository.getByKey(book.getKey())).isNull();
        assertThat(repository.getByKey(changes.getKey()).getContent()).isEqualTo(book.getContent());
    }

    @Test
    void renamingOntoAnotherBookIsAConflict() {
        Book dracula = TestBooks.book("Dracula", "Bram Stoker");
        Book emma = TestBooks.book("Emma", "Jane Austen");
        repository.create(dracula);
        repository.create(emma);
        Book changes = new Book("Emma", "Jane Austen", "English", emma.getReleaseDate(), 0);

        assertThatThrownBy(() -> service.update(dracula.getKey(), changes))
                .isInstanceOf(BookService.BookConflictException.class);
        assertThat(repository.getAll()).hasSize(2);
    }

    @Test
    void editingAMissingBookGivesNull() throws Exception {
        assertThat(service.update("missing", TestBooks.book("X", "Y"))).isNull();
    }
}
