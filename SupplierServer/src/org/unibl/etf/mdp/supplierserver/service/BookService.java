package org.unibl.etf.mdp.supplierserver.service;

import org.unibl.etf.mdp.common.Json;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.supplierserver.logger.FileLogger;
import org.unibl.etf.mdp.supplierserver.properties.AppConfig;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Supplier books: downloaded from Project Gutenberg or entered by hand, stored as a text file plus cover. */
public class BookService {
    private static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(BookService.class.getName());
    private static final String EXT = conf.getBookExt();
    private static final String IMAGE_EXT = conf.getImageExt();
    private static final String IMAGE_URL_END = conf.getImageUrlEnd();
    private static final String LINKS_FILE = conf.getLinksFile();
    private static final int PREVIEW_LINES = conf.getPreviewLines();
    private static final String START_MARKER = conf.getStartMarker();
    private static final String METADATA_EXT = ".json";
    // Gutenberg headers write the date as "June 1, 1998" or, in older books, "June, 1998"
    private static final Pattern RELEASE_DAY = Pattern.compile(
            "(?im)^\\s*Release date:\\s*([A-Za-z]+\\s+\\d{1,2},\\s*\\d{4})");
    private static final Pattern RELEASE_MONTH = Pattern.compile(
            "(?im)^\\s*Release date:\\s*([A-Za-z]+),?\\s+(\\d{4})");

    private final Path directory;

    public BookService() {
        this(Paths.get(conf.getSuppliersDir()));
    }

    public BookService(Path directory) {
        this.directory = directory;
    }

    /** The supplier's Gutenberg links, one per line, without blanks, comments or repeats. */
    public List<String> readLinks(String supplier) throws IOException {
        Path file = directory.resolve(supplier).resolve(LINKS_FILE);
        if (Files.notExists(file)) {
            throw new IOException("No " + LINKS_FILE + " for " + supplier + " at " + file.toAbsolutePath());
        }
        Set<String> links = new LinkedHashSet<>();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String link = line.trim();
            if (!link.isEmpty() && !link.startsWith("#")) {
                links.add(link);
            }
        }
        return new ArrayList<>(links);
    }

    /**
     * Downloads a book and its cover. The cover link is not stored anywhere: it is derived from the book link
     * ({@code pg123.txt} becomes {@code pg123.cover.medium.jpg}). A missing cover leaves the book without one.
     *
     * @throws IOException when the text itself cannot be downloaded or is not a Gutenberg book
     */
    public Book getBookFromUrl(String url) throws IOException {
        String content;
        try (InputStream in = new URL(url).openStream()) {
            content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        Book book = parse(content);
        if (book.getTitle() == null || book.getAuthor() == null) {
            throw new IOException("Not a Project Gutenberg book (no Title/Author header): " + url);
        }
        book.setCoverImageBase64(downloadCover(coverUrl(url)));
        return book;
    }

    static String coverUrl(String bookUrl) {
        int lastSlash = bookUrl.lastIndexOf('/');
        int dot = bookUrl.lastIndexOf('.');
        String name = dot > lastSlash ? bookUrl.substring(lastSlash + 1, dot) : bookUrl.substring(lastSlash + 1);
        return bookUrl.substring(0, lastSlash + 1) + name + IMAGE_URL_END;
    }

    private static String downloadCover(String coverUrl) {
        try {
            BufferedImage image = ImageIO.read(new URL(coverUrl));
            if (image == null) {
                logger.warning("No cover image at " + coverUrl);
                return null;
            }
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ImageIO.write(image, IMAGE_EXT, bytes);
            return Base64.getEncoder().encodeToString(bytes.toByteArray());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not download the cover " + coverUrl, e);
            return null;
        }
    }

    private static Book parse(String content) {
        Book book = new Book();
        book.setContent(content);
        book.setTitle(parsePattern("(?im)^\\s*Title:\\s*(.*)$", content));
        book.setAuthor(parsePattern("(?im)^\\s*Author:\\s*(.*)$", content));
        book.setLanguage(parsePattern("(?im)^\\s*Language:\\s*(.*)$", content));
        book.setReleaseDate(parseReleaseDate(content));
        return book;
    }

    static Date parseReleaseDate(String content) {
        try {
            Matcher day = RELEASE_DAY.matcher(content);
            if (day.find()) {
                return new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).parse(day.group(1).replaceAll("\\s+", " "));
            }
            Matcher month = RELEASE_MONTH.matcher(content);
            if (month.find()) {
                return new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).parse(month.group(1) + " " + month.group(2));
            }
        } catch (ParseException e) {
            logger.log(Level.WARNING, "Unreadable release date", e);
        }
        return null;
    }

    private static String parsePattern(String pattern, String text) {
        Matcher m = Pattern.compile(pattern).matcher(text);
        if (m.find()) {
            String value = m.group(1).trim();
            return value.isEmpty() ? null : value;
        }
        return null;
    }

    /** The first lines after Gutenberg's start marker, or the beginning of a text that has none. */
    public static String preview(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String[] lines = content.split("\n");
        int start = 0;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(START_MARKER)) {
                start = i + 1;
                break;
            }
        }
        StringBuilder result = new StringBuilder();
        for (int i = start; i < lines.length && i < start + PREVIEW_LINES; i++) {
            result.append(lines[i]).append("\n");
        }
        return result.toString();
    }

    public BookDto toDto(Book book) {
        BookDto dto = new BookDto(book);
        dto.setPreview(preview(book.getContent()));
        return dto;
    }

    /** Adds a link to the supplier's linkovi.txt (once), so the book stays in the offer after a restart. */
    public void rememberLink(String supplier, String link) throws IOException {
        if (readLinksOrEmpty(supplier).contains(link.trim())) {
            return;
        }
        Path file = Files.createDirectories(directory.resolve(supplier)).resolve(LINKS_FILE);
        String prefix = Files.exists(file) && !Files.readString(file, StandardCharsets.UTF_8).endsWith("\n")
                && Files.size(file) > 0 ? "\n" : "";
        Files.writeString(file, prefix + link.trim() + "\n", StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private List<String> readLinksOrEmpty(String supplier) throws IOException {
        try {
            return readLinks(supplier);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /** Saves a book entered by hand together with its metadata, which its text does not contain. */
    public void saveEnteredBook(Book book, String supplier) throws IOException {
        saveBook(book, supplier);
        BookDto metadata = new BookDto(book);
        metadata.setCoverImageBase64(null);
        Files.writeString(bookFile(supplier, book.toString(), METADATA_EXT), Json.toJson(metadata),
                StandardCharsets.UTF_8);
    }

    /** The books the supplier entered by hand, read back from their metadata files. */
    public List<BookDto> enteredBooks(String supplier) throws IOException {
        List<BookDto> books = new ArrayList<>();
        Path dir = directory.resolve(supplier);
        if (Files.notExists(dir)) {
            return books;
        }
        List<Path> metadataFiles;
        try (Stream<Path> files = Files.list(dir)) {
            metadataFiles = files.filter(f -> f.toString().endsWith(METADATA_EXT)).sorted().collect(Collectors.toList());
        }
        for (Path file : metadataFiles) {
            BookDto metadata = Json.fromJson(Files.readString(file, StandardCharsets.UTF_8), BookDto.class);
            Book book = readBook(metadata, supplier);
            if (book != null) {
                books.add(toDto(book));
            }
        }
        return books;
    }

    /** Saves the text and, if there is one, the cover next to it. */
    public void saveBook(Book book, String supplier) throws IOException {
        Path textFile = bookFile(supplier, book.toString(), EXT);
        Files.createDirectories(textFile.getParent());
        Files.writeString(textFile, book.getContent(), StandardCharsets.UTF_8);
        byte[] cover = book.getCoverImageBytes();
        if (cover != null) {
            Files.write(bookFile(supplier, book.toString(), "." + IMAGE_EXT), cover);
        }
    }

    /** The full book as the supplier stored it, cover included; null when it is not there. */
    public Book readBook(BookDto bookDto, String supplier) throws IOException {
        Path textFile = bookFile(supplier, bookDto.toString(), EXT);
        if (Files.notExists(textFile)) {
            return null;
        }
        Book book = parse(Files.readString(textFile, StandardCharsets.UTF_8));
        // Metadata of hand-entered books is not in the text; the offer is the reference
        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setLanguage(bookDto.getLanguage());
        book.setReleaseDate(bookDto.getReleaseDate());
        book.setPrice(bookDto.getPrice());
        Path coverFile = bookFile(supplier, bookDto.toString(), "." + IMAGE_EXT);
        if (Files.exists(coverFile)) {
            book.setCoverImageBase64(Base64.getEncoder().encodeToString(Files.readAllBytes(coverFile)));
        }
        return book;
    }

    private Path bookFile(String supplier, String bookName, String extension) {
        return directory.resolve(supplier).resolve(bookName + extension);
    }
}
