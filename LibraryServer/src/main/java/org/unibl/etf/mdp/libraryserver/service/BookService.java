package org.unibl.etf.mdp.libraryserver.service;

import org.unibl.etf.mdp.libraryserver.logger.FileLogger;
import org.unibl.etf.mdp.libraryserver.properties.AppConfig;
import org.unibl.etf.mdp.libraryserver.repository.BookRepository;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookService {
    private static final AppConfig conf = new AppConfig();
    private static final int PREVIEW_LINES = conf.getPreviewLines();
    private static final String START_MARKER = conf.getStartMarker();
    private static final Logger logger = FileLogger.getLogger(BookService.class.getName());

    private final BookRepository repository;

    public BookService() {
        this(BookRepository.getInstance());
    }

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public BookDto add(Book book) {
        BookDto bookDto = null;
        if (book == null || book.getTitle() == null || book.getTitle().isBlank() || book.getAuthor() == null
                || book.getAuthor().isBlank() || book.getLanguage() == null || book.getLanguage().isBlank()
                || book.getContent() == null || book.getContent().isBlank()) {
            logger.log(Level.SEVERE, "Uncomplete book");
            return bookDto;
        }
        repository.create(book);
        bookDto = new BookDto(book);
        bookDto.setPreview(getPreview(book.getContent()));
        return bookDto;
    }

    /** Thrown when an edit would turn a book into another one that already exists. */
    public static class BookConflictException extends Exception {
        public BookConflictException(String message) {
            super(message);
        }
    }

    /**
     * Changes a book. Text and cover are optional: when the edit leaves them out the stored ones are kept.
     * Title, author, language and date form the key, so changing them moves the book to a new key.
     *
     * @return the updated book, or null when there is no book with this key
     */
    public BookDto update(String key, Book changes) throws BookConflictException {
        if (changes == null || isBlank(changes.getTitle()) || isBlank(changes.getAuthor())
                || isBlank(changes.getLanguage()) || changes.getPrice() < 0) {
            throw new IllegalArgumentException("Title, author and language are required and the price cannot be negative");
        }
        Book existing = repository.getByKey(key);
        if (existing == null) {
            return null;
        }
        if (isBlank(changes.getContent())) {
            changes.setContent(existing.getContent());
        }
        if (isBlank(changes.getCoverImageBase64())) {
            changes.setCoverImageBase64(existing.getCoverImageBase64());
        }
        String newKey = changes.getKey();
        if (!newKey.equals(key)) {
            if (repository.getByKey(newKey) != null) {
                throw new BookConflictException("Another book with this title, author, language and date exists");
            }
            repository.create(changes);
            repository.deleteByKey(key);
        } else {
            repository.create(changes);
        }
        BookDto dto = new BookDto(changes);
        dto.setPreview(getPreview(changes.getContent()));
        return dto;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public boolean delete(String key) {
        return repository.deleteByKey(key);
    }

    public Book getByKey(String key) {
        return repository.getByKey(key);
    }

    public List<BookDto> getAll() {
        List<BookDto> bookDtos = new ArrayList<>();
        List<Book> books = repository.getAll();
        for (Book book : books) {
            BookDto bookDto = new BookDto(book);
            bookDto.setPreview(getPreview(book.getContent()));
            bookDtos.add(bookDto);
        }
        return bookDtos;
    }

    private String getPreview(String content) {
        if (content == null || content.isBlank()) {
            return "No content available.";
        }
        String[] lines = content.split("\n");
        StringBuilder result = new StringBuilder();
        boolean startFound = false;
        int linesRead = 0;

        for (String line : lines) {
            if (!startFound) {
                if (line.contains(START_MARKER)) {
                    startFound = true;
                }
                continue;
            }

            result.append(line).append("\n");
            linesRead++;
            if (linesRead >= PREVIEW_LINES) {
                break;
            }
        }
        return result.toString().trim();

    }
}
