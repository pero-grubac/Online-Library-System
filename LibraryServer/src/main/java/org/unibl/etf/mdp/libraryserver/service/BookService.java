package org.unibl.etf.mdp.libraryserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.libraryserver.logger.FileLogger;
import org.unibl.etf.mdp.libraryserver.properties.AppConfig;
import org.unibl.etf.mdp.libraryserver.repository.BookRepository;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;

public class BookService {
	private static final AppConfig conf = new AppConfig();
	private static final int PREVIEW_LINES = conf.getPreviewLines();
	private static final String START_MARKER = conf.getStartMarker();
	private static final Logger logger = FileLogger.getLogger(BookService.class.getName());

	private final BookRepository repository = BookRepository.getInstance();

	public BookService() {

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
