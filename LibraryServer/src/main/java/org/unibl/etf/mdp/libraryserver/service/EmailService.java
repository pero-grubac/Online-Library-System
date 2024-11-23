package org.unibl.etf.mdp.libraryserver.service;

import java.util.List;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.unibl.etf.mdp.libraryserver.logger.FileLogger;
import org.unibl.etf.mdp.libraryserver.properties.AppConfig;
import org.unibl.etf.mdp.model.BookDto;

public class EmailService {
	private static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(EmailService.class.getName());
	private static final String HOST = conf.getMailHost();
	private static final int PORT = conf.getMailPort();
	private static final String USERNAME = conf.getMailUser();
	private static final String PASSWORD = conf.getMailPass();
	private static final String KEY_STORE_PATH = conf.getKeyStorePath();
	private static final String KEY_STORE_PASSWORD = conf.getKeyStorePass();

	public void sendEmail(String zipFilePath, String to, List<BookDto> books) {
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

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(USERNAME));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject("Your Requested File");

			MimeBodyPart messageBodyPart = new MimeBodyPart();
			StringBuilder body = new StringBuilder(
					"Hello,\n\nPlease find the requested file attached.\n\nList of books:\n");
			for (BookDto book : books) {
				body.append("- ").append(book.getTitle()).append(" by ").append(book.getAuthor()).append("\n");
			}
			messageBodyPart.setText(body.toString());

			MimeBodyPart attachmentPart = new MimeBodyPart();
			DataSource source = new FileDataSource(new File(zipFilePath));
			attachmentPart.setDataHandler(new DataHandler(source));
			attachmentPart.setFileName(new File(zipFilePath).getName());

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			multipart.addBodyPart(attachmentPart);

			message.setContent(multipart);

			Transport.send(message);

			logger.info("Email sent successfully to " + to);
		} catch (MessagingException e) {
			logger.log(Level.SEVERE, "Error while sending email: " + e.getMessage(), e);
		}
	}
}
