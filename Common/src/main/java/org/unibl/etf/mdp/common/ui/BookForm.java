package org.unibl.etf.mdp.common.ui;

import org.unibl.etf.mdp.model.Book;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * A dialog to enter or edit a book: title, author, language, release date, price, cover image and text file.
 * Used by the library (book CRUD) and by suppliers (books entered by hand).
 */
public class BookForm extends JDialog {
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private final JTextField title = new JTextField(24);
    private final JTextField author = new JTextField(24);
    private final JTextField language = new JTextField("English", 24);
    private final JTextField releaseDate = new JTextField(24);
    private final JSpinner price = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
    private final JLabel coverName = new JLabel("No file chosen");
    private final JLabel textName = new JLabel("No file chosen");
    private final boolean textRequired;
    private String coverBase64;
    private String content;
    private Book result;

    /**
     * @param existing     the book to edit, or null for a new one
     * @param textRequired a new book needs its text; an edit keeps the stored one unless a new file is chosen
     * @param withPrice    suppliers set the price per order, so they do not see this field
     */
    public BookForm(Window owner, String heading, Book existing, boolean textRequired, boolean withPrice) {
        super(owner, heading, ModalityType.APPLICATION_MODAL);
        this.textRequired = textRequired;
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        int row = 0;
        row = addRow(fields, c, row, "Title:", title);
        row = addRow(fields, c, row, "Author:", author);
        row = addRow(fields, c, row, "Language:", language);
        row = addRow(fields, c, row, "Release date (" + DATE_PATTERN + "):", releaseDate);
        if (withPrice) {
            row = addRow(fields, c, row, "Price:", price);
        }
        row = addRow(fields, c, row, "Cover image:", filePicker(coverName, this::chooseCover));
        addRow(fields, c, row, "Text file (.txt):", filePicker(textName, this::chooseText));

        if (existing != null) {
            title.setText(existing.getTitle());
            author.setText(existing.getAuthor());
            language.setText(existing.getLanguage());
            if (existing.getReleaseDate() != null) {
                releaseDate.setText(new SimpleDateFormat(DATE_PATTERN).format(existing.getReleaseDate()));
            }
            price.setValue(existing.getPrice());
            coverName.setText(existing.getCoverImageBase64() != null ? "Current cover (keep)" : "No cover");
            textName.setText("Current text (keep)");
        }

        JButton save = new JButton("Save");
        save.addActionListener(e -> save());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(save);
        buttons.add(cancel);

        getContentPane().add(fields, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(save);
        pack();
        setLocationRelativeTo(owner);
    }

    private static int addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        panel.add(field, c);
        return row + 1;
    }

    private static JPanel filePicker(JLabel name, Runnable choose) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton browse = new JButton("Browse...");
        browse.addActionListener(e -> choose.run());
        panel.add(browse);
        panel.add(Box.createHorizontalStrut(8));
        panel.add(name);
        return panel;
    }

    private void chooseCover() {
        File file = BookViews.chooseFile(this, "Images", "png", "jpg", "jpeg", "gif", "bmp");
        if (file != null) {
            try {
                coverBase64 = BookViews.readCover(file);
                coverName.setText(file.getName());
            } catch (IOException e) {
                BookViews.error(this, e.getMessage());
            }
        }
    }

    private void chooseText() {
        File file = BookViews.chooseFile(this, "Text files", "txt");
        if (file != null) {
            try {
                content = BookViews.readText(file);
                textName.setText(file.getName() + " (" + content.lines().count() + " lines)");
            } catch (IOException e) {
                BookViews.error(this, "Cannot read " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    private void save() {
        if (title.getText().isBlank() || author.getText().isBlank() || language.getText().isBlank()) {
            BookViews.error(this, "Title, author and language are required.");
            return;
        }
        if (textRequired && content == null) {
            BookViews.error(this, "Choose the text file of the book.");
            return;
        }
        Book book = new Book(title.getText().trim(), author.getText().trim(), language.getText().trim(), null,
                (Integer) price.getValue());
        if (!releaseDate.getText().isBlank()) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
                format.setLenient(false);
                book.setReleaseDate(format.parse(releaseDate.getText().trim()));
            } catch (ParseException e) {
                BookViews.error(this, "Release date must look like 1897-05-26.");
                return;
            }
        }
        book.setContent(content);
        book.setCoverImageBase64(coverBase64);
        result = book;
        dispose();
    }

    /** Shows the dialog; returns the entered book, or null when cancelled. */
    public Book showDialog() {
        setVisible(true);
        return result;
    }
}
