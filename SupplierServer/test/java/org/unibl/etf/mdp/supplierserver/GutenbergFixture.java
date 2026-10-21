package org.unibl.etf.mdp.supplierserver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Local copies of Project Gutenberg books in the same layout as the real site:
 * {@code pg<id>.txt} next to {@code pg<id>.cover.medium.jpg}, so no network is needed.
 */
public final class GutenbergFixture {
    public static final String TITLE = "Pride and Prejudice";
    public static final String AUTHOR = "Jane Austen";

    private GutenbergFixture() {
    }

    public static String create(Path dir, String title) throws IOException {
        return create(dir, 1342, title, "Release date: June 1, 1998 [eBook #1342]", true);
    }

    /** @return the file:// link of the book */
    public static String create(Path dir, int id, String title, String releaseLine, boolean withCover)
            throws IOException {
        StringBuilder text = new StringBuilder()
                .append("The Project Gutenberg eBook of ").append(title).append("\n\n")
                .append("Title: ").append(title).append("\n")
                .append("Author: ").append(AUTHOR).append("\n")
                .append(releaseLine).append("\n")
                .append("Language: English\n\n")
                .append("*** START OF THE PROJECT GUTENBERG EBOOK ").append(title.toUpperCase()).append(" ***\n");
        for (int i = 1; i <= 150; i++) {
            text.append("line ").append(i).append("\n");
        }
        Path book = dir.resolve("pg" + id + ".txt");
        Files.writeString(book, text, StandardCharsets.UTF_8);

        if (withCover) {
            BufferedImage cover = new BufferedImage(4, 6, BufferedImage.TYPE_INT_RGB);
            ImageIO.write(cover, "png", dir.resolve("pg" + id + ".cover.medium.jpg").toFile());
        }
        return book.toUri().toString();
    }
}
