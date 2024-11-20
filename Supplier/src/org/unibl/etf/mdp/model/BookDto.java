package org.unibl.etf.mdp.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;

public class BookDto implements Serializable {
	private static final long serialVersionUID = 1L;
	private String title;
	private String author;
	private String language;
	private Date releaseDate;
	private String preview;
	private int price;
	private String coverImageBase64;

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
		this.coverImageBase64 = book.getCoverImageBase64();
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

	public String getCoverImageBase64() {
		return coverImageBase64;
	}

	public void setCoverImageBase64(String coverImageBase64) {
		this.coverImageBase64 = coverImageBase64;
	}

	@Override
	public int hashCode() {
		return Objects.hash(author, language, price, releaseDate, title);
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
		String sanitizedAuthor = sanitize(author);
		String sanitizedTitle = sanitize(title);
		String sanitizedLanguage = sanitize(language);
		String sanitizedDate = getFormatedDate().replaceAll("[^a-zA-Z0-9]", "");

		return (sanitizedAuthor + ":" + sanitizedTitle + ":" + sanitizedLanguage + ":" + sanitizedDate).toLowerCase();
	}

	private String sanitize(String input) {
		if (input == null) {
			return "";
		}
		return input.replaceAll("[^a-zA-Z0-9 ]", "").replaceAll(" +", "_").trim();
	}

	public String getFormatedDate() {
		SimpleDateFormat displayFormat = new SimpleDateFormat("dd.MM.yyyy.");
		return (releaseDate != null) ? displayFormat.format(releaseDate) : "N/A";
	}

	public byte[] getCoverImageBytes() {
		byte[] image = null;
		if (coverImageBase64 != null && !coverImageBase64.isEmpty())
			image = Base64.getDecoder().decode(coverImageBase64);
		return image;
	}
}
