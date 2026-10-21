package org.unibl.etf.mdp.common.ui;

import org.unibl.etf.mdp.model.BookDto;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Swing pieces shared by the Library, User and Supplier applications. */
public final class BookViews {
    private static final Logger logger = Logger.getLogger(BookViews.class.getName());

    private BookViews() {
    }

    /** The cover scaled to the given size, or a "No cover" placeholder. */
    public static JLabel coverLabel(byte[] imageBytes, int width, int height) {
        JLabel label = new JLabel("No cover", SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(width, height));
        label.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        if (imageBytes != null) {
            try {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
                if (image != null) {
                    label.setText(null);
                    label.setIcon(new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "Unreadable cover image", e);
            }
        }
        return label;
    }

    /** A panel with the cover on the left, the book's data in the middle and the given buttons on the right. */
    public static JPanel bookRow(BookDto book, JComponent... buttons) {
        // A white card on the window's grey background
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6),
                        BorderFactory.createLineBorder(new Color(0xe3e6eb))),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        row.add(coverLabel(book.getCoverImageBytes(), 70, 105), BorderLayout.WEST);

        JPanel attributes = new JPanel();
        attributes.setOpaque(false);
        attributes.setLayout(new BoxLayout(attributes, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(book.getTitle());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        attributes.add(title);
        attributes.add(Box.createVerticalStrut(4));
        attributes.add(new JLabel(book.getAuthor()));
        Color muted = new Color(0x6b7280);
        for (String line : new String[]{"Language: " + book.getLanguage(), "Released: " + book.getFormatedDate()}) {
            JLabel label = new JLabel(line);
            label.setForeground(muted);
            attributes.add(label);
        }
        row.add(attributes, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        for (JComponent button : buttons) {
            actions.add(button);
        }
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    /** Title, author, cover and the preview text, as the assignment's "Details" window asks. */
    public static void showDetails(Component parent, BookDto book) {
        JPanel panel = new JPanel(new BorderLayout(16, 12));
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(book.getTitle());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        header.add(title);
        header.add(new JLabel(book.getAuthor() + "  ·  " + book.getLanguage() + "  ·  " + book.getFormatedDate()));
        panel.add(header, BorderLayout.NORTH);
        panel.add(coverLabel(book.getCoverImageBytes(), 150, 225), BorderLayout.WEST);

        JTextArea preview = new JTextArea(book.getPreview() == null || book.getPreview().isBlank()
                ? "No preview available." : book.getPreview());
        preview.setEditable(false);
        preview.setLineWrap(true);
        preview.setWrapStyleWord(true);
        preview.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(preview);
        scroll.setPreferredSize(new Dimension(460, 320));
        panel.add(scroll, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(parent, panel, "Details", JOptionPane.PLAIN_MESSAGE);
    }

    public static File chooseFile(Component parent, String description, String... extensions) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(description, extensions));
        return chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
    }

    public static String readText(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    /** Any image ImageIO can read, re-encoded as PNG and Base64 for transport. */
    public static String readCover(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException(file.getName() + " is not an image");
        }
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(image, "png", png);
        return Base64.getEncoder().encodeToString(png.toByteArray());
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    /** "1 book", "3 books". */
    public static String books(int count) {
        return count + (count == 1 ? " book" : " books");
    }
}
