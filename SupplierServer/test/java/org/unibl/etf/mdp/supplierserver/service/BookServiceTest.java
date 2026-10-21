package org.unibl.etf.mdp.supplierserver.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.supplierserver.GutenbergFixture;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookServiceTest {
    private static final String SUPPLIER = "BookSupplier_T";

    @TempDir
    Path store;
    @TempDir
    Path gutenberg;

    private BookService service() {
        return new BookService(store);
    }

    private static String day(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    @Test
    void readsMetadataFromTheGutenbergHeader() throws Exception {
        Book book = service().getBookFromUrl(GutenbergFixture.create(gutenberg, GutenbergFixture.TITLE));

        assertThat(book.getTitle()).isEqualTo(GutenbergFixture.TITLE);
        assertThat(book.getAuthor()).isEqualTo(GutenbergFixture.AUTHOR);
        assertThat(book.getLanguage()).isEqualTo("English");
        assertThat(day(book.getReleaseDate())).isEqualTo("1998-06-01");
        assertThat(book.getContent()).contains("line 150");
        // The price is set per order by the supplier, not at download time
        assertThat(book.getPrice()).isZero();
    }

    @Test
    void releaseDateIsReadInEveryGutenbergSpelling() {
        assertThat(day(BookService.parseReleaseDate("Release date: June 1, 1998 [eBook #1342]"))).isEqualTo("1998-06-01");
        assertThat(day(BookService.parseReleaseDate("Release Date: January 12, 2004 [EBook #10]"))).isEqualTo("2004-01-12");
        assertThat(day(BookService.parseReleaseDate("RELEASE DATE: March 3, 2010"))).isEqualTo("2010-03-03");
        // Older headers have only month and year
        assertThat(day(BookService.parseReleaseDate("Release Date: June, 1998 [EBook #1342]"))).isEqualTo("1998-06-01");
        assertThat(BookService.parseReleaseDate("No date here")).isNull();
    }

    @Test
    void coverLinkIsDerivedFromTheBookLink() {
        assertThat(BookService.coverUrl("https://www.gutenberg.org/cache/epub/24022/pg24022.txt"))
                .isEqualTo("https://www.gutenberg.org/cache/epub/24022/pg24022.cover.medium.jpg");
    }

    @Test
    void downloadsTheCoverNextToTheBook() throws Exception {
        Book book = service().getBookFromUrl(GutenbergFixture.create(gutenberg, GutenbergFixture.TITLE));

        assertThat(book.getCoverImageBytes()).isNotEmpty();
    }

    @Test
    void missingCoverStillGivesTheBook() throws Exception {
        String url = GutenbergFixture.create(gutenberg, 84, "Frankenstein", "Release date: October 31, 1993", false);

        Book book = service().getBookFromUrl(url);

        assertThat(book.getTitle()).isEqualTo("Frankenstein");
        assertThat(book.getCoverImageBase64()).isNull();
    }

    @Test
    void missingOrForeignTextIsAnError() throws Exception {
        Path notABook = gutenberg.resolve("pg1.txt");
        Files.writeString(notABook, "just some text");

        assertThatThrownBy(() -> service().getBookFromUrl(gutenberg.resolve("pg404.txt").toUri().toString()))
                .isInstanceOf(IOException.class);
        assertThatThrownBy(() -> service().getBookFromUrl(notABook.toUri().toString()))
                .isInstanceOf(IOException.class).hasMessageContaining("Not a Project Gutenberg book");
    }

    @Test
    void storedBookComesBackWithItsCoverAndTheOfferedData() throws Exception {
        BookService service = service();
        Book book = service.getBookFromUrl(GutenbergFixture.create(gutenberg, GutenbergFixture.TITLE));
        service.saveBook(book, SUPPLIER);
        BookDto offered = service.toDto(book);
        offered.setPrice(25);

        Book stored = service.readBook(offered, SUPPLIER);

        assertThat(stored).isEqualTo(book);
        assertThat(stored.getContent()).isEqualTo(book.getContent());
        assertThat(stored.getCoverImageBytes()).isEqualTo(book.getCoverImageBytes());
        assertThat(stored.getPrice()).isEqualTo(25);
    }

    @Test
    void handEnteredBookKeepsItsMetadata() throws Exception {
        BookService service = service();
        Book book = new Book("My Notes", "A. Writer", "Serbian", null, 0);
        book.setContent("Prvi red\nDrugi red\n");
        book.setCoverImageBase64(Base64.getEncoder().encodeToString(new byte[]{1, 2, 3}));
        service.saveBook(book, SUPPLIER);

        Book stored = service.readBook(service.toDto(book), SUPPLIER);

        assertThat(stored.getTitle()).isEqualTo("My Notes");
        assertThat(stored.getLanguage()).isEqualTo("Serbian");
        assertThat(stored.getCoverImageBytes()).containsExactly(1, 2, 3);
        assertThat(service.toDto(book).getPreview()).isEqualTo("Prvi red\nDrugi red\n");
    }

    @Test
    void unknownBookIsNull() throws Exception {
        BookDto unknown = new BookDto();
        unknown.setTitle("Nope");

        assertThat(service().readBook(unknown, SUPPLIER)).isNull();
    }

    @Test
    void linksAreReadWithoutBlanksCommentsOrRepeats() throws Exception {
        Path dir = Files.createDirectories(store.resolve(SUPPLIER));
        Files.writeString(dir.resolve("linkovi.txt"), "# comment\nhttp://a/pg1.txt\n\n  http://b/pg2.txt  \nhttp://a/pg1.txt\n",
                StandardCharsets.UTF_8);

        assertThat(service().readLinks(SUPPLIER)).containsExactly("http://a/pg1.txt", "http://b/pg2.txt");
        assertThatThrownBy(() -> service().readLinks("Nobody")).isInstanceOf(IOException.class);
    }

    @Test
    void previewWithoutStartMarkerIsTheBeginning() {
        assertThat(BookService.preview("a\nb\n")).isEqualTo("a\nb\n");
        assertThat(BookService.preview(null)).isEmpty();
    }
}
