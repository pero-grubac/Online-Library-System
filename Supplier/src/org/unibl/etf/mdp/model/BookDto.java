package org.unibl.etf.mdp.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

import javax.imageio.ImageIO;

import org.unibl.etf.mdp.supplier.properties.AppConfig;

public class BookDto implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Random rand = new Random();
	private static final AppConfig conf = new AppConfig();

	private String title;
	private String author;
	private String language;
	private Date releaseDate;
	private String preview;
	private int price;
	private byte[] coverImageBytes;
	private String imageUrl;

	public BookDto() {
		super();
	}

	public BookDto(String title, String author, String language, Date releaseDate, int price, String preview) {
		super();
		this.title = title;
		this.author = author;
		this.language = language;
		this.releaseDate = releaseDate;
		this.price = price;
		this.preview = preview;
	}

	public BookDto(Book book) {
		this.title = book.getTitle();
		this.author = book.getAuthor();
		this.language = book.getLanguage();
		this.releaseDate = book.getReleaseDate();
		this.price = book.getPrice();
		this.coverImageBytes = book.getCoverImageBytes();
		this.imageUrl = book.getImageUrl();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public String getPreview() {
		return preview;
	}

	public void setPreview(String preview) {
		this.preview = preview;
	}

	public byte[] getCoverImageBytes() {
		return coverImageBytes;
	}

	public void setCoverImageBytes(BufferedImage coverImage) {
		this.coverImageBytes = getCoverImageAsBytes(coverImage);
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	@Override
	public int hashCode() {
		return Objects.hash(author, language, price, releaseDate, title);
	}

	public byte[] getCoverImageAsBytes(BufferedImage coverImage) {
		if (coverImage == null) {
			System.err.println("Cover image is null. Cannot convert to bytes.");
			return null;
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageIO.write(coverImage, conf.getImageExt(), baos);
			return baos.toByteArray();
		} catch (Exception e) {
			System.err.println("Error while converting cover image to bytes: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public BufferedImage getCoverImageFromBytes() {
		if (coverImageBytes == null) {
			System.err.println("Cover image bytes are null. Cannot convert to BufferedImage.");
			return null;
		}
		try (ByteArrayInputStream bais = new ByteArrayInputStream(coverImageBytes)) {
			return ImageIO.read(bais);
		} catch (Exception e) {
			System.err.println("Error while converting bytes to cover image: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BookDto other = (BookDto) obj;
		return Objects.equals(author, other.author) && Objects.equals(language, other.language) && price == other.price
				&& Objects.equals(releaseDate, other.releaseDate) && Objects.equals(title, other.title);
	}

	@Override
	public String toString() {
		String result = author + " - " + title + " [" + language + "] (" + getFormatedDate() + ")";

		return result.replaceAll("[:\\\\/*?|<>]", "-");
	}

	public String getKey() {

		return (author + ":" + title + ":" + language + ":" + getFormatedDate()).toLowerCase();
	}

	public String getFormatedDate() {
		SimpleDateFormat displayFormat = new SimpleDateFormat("dd.MM.yyyy.");
		return (releaseDate != null) ? displayFormat.format(releaseDate) : "N/A";
	}
}
