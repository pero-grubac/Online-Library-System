package org.unibl.etf.mdp.libraryserver.service;

import org.unibl.etf.mdp.libraryserver.logger.FileLogger;
import org.unibl.etf.mdp.libraryserver.properties.AppConfig;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipService {
    private static final Logger logger = FileLogger.getLogger(ZipService.class.getName());
    private static final AppConfig conf = new AppConfig();
    private static final String BOOK_EXT = conf.getBookExt();
    private static final String ZIP_FOLDER = conf.getZipFolder();
    private final BookService bookService;

    public ZipService() {
        this(new BookService());
    }

    public ZipService(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Packs the full texts into a new ZIP file of its own, so concurrent requests never share one.
     * The caller deletes the file when it is done with it.
     *
     * @return the file, or null when none of the books exists
     */
    public Path zipBooks(List<BookDto> bookDtos) throws IOException {
        Path folder = Files.createDirectories(Paths.get(ZIP_FOLDER));
        Path zipFile = Files.createTempFile(folder, "books-", ".zip");
        int added = 0;
        Set<String> names = new HashSet<>();
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            for (BookDto bookDto : bookDtos) {
                Book book = bookService.getByKey(bookDto.getKey());
                if (book == null) {
                    logger.warning("Book not found for key: " + bookDto.getKey());
                    continue;
                }
                String name = book.getTitle().replaceAll("[:\\\\/*?|<>\"]", "-");
                // Two books with the same title get distinct entries
                String fileName = name + BOOK_EXT;
                for (int i = 2; !names.add(fileName); i++) {
                    fileName = name + " (" + i + ")" + BOOK_EXT;
                }
                zipOut.putNextEntry(new ZipEntry(fileName));
                String content = book.getContent() != null ? book.getContent() : "No content available.";
                zipOut.write(content.getBytes(StandardCharsets.UTF_8));
                zipOut.closeEntry();
                added++;
            }
        }
        if (added == 0) {
            Files.deleteIfExists(zipFile);
            return null;
        }
        logger.info("ZIP file created: " + zipFile.toAbsolutePath());
        return zipFile;
    }
}
