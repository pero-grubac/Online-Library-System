package org.unibl.etf.mdp.libraryserver.service;

import org.unibl.etf.mdp.libraryserver.logger.FileLogger;
import org.unibl.etf.mdp.libraryserver.properties.AppConfig;
import org.unibl.etf.mdp.model.BookDto;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class EmailService {
    private static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(EmailService.class.getName());
    private static final String HOST = conf.getMailHost();
    private static final int PORT = conf.getMailPort();
    private static final String USERNAME = conf.getMailUser();
    private static final String PASSWORD = conf.getMailPass();

    /** False when no mailbox is configured, so nothing can be sent. */
    public boolean isConfigured() {
        return USERNAME != null && !USERNAME.isBlank() && PASSWORD != null && !PASSWORD.isBlank();
    }

    /** The text of the mail: every book with its details. */
    public static String body(List<BookDto> books) {
        StringBuilder body = new StringBuilder("Hello,\n\nThe books you requested are attached as a ZIP archive.\n\n");
        int n = 1;
        for (BookDto book : books) {
            body.append(n++).append(". ").append(book.getTitle()).append("\n")
                    .append("   Author:       ").append(book.getAuthor()).append("\n")
                    .append("   Language:     ").append(book.getLanguage()).append("\n")
                    .append("   Release date: ").append(book.getFormatedDate()).append("\n");
        }
        body.append("\nNumber of books: ").append(books.size()).append("\n\nOnline Library\n");
        return body.toString();
    }

    /** @throws MessagingException when the mail could not be sent; the caller must not report success */
    public void sendEmail(Path zipFile, String to, List<BookDto> books) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(USERNAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("Your books from the Online Library");

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body(books), "UTF-8");

        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setDataHandler(new DataHandler(new FileDataSource(zipFile.toFile())));
        attachmentPart.setFileName("books.zip");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        multipart.addBodyPart(attachmentPart);
        message.setContent(multipart);

        Transport.send(message);
        logger.info("Email sent successfully to " + to);
    }
}
