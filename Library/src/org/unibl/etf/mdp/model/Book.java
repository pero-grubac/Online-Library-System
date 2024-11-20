package org.unibl.etf.mdp.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class Book implements Serializable {

	private static final long serialVersionUID = 1L;

	private String title;
	private String author;
	private String language;
	private Date releaseDate;
	private String content;
	private int price;
	private String coverImageBase64;

	public Book() {
		super();
	}

	public Book(String title, String author, String language, Date realeaseDate,int price) {
		super();
		this.title = title;
		this.author = author;
		this.language = language;
		this.releaseDate = realeaseDate;
		this.price = price;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public String getCoverImageBase64() {
		return coverImageBase64;
	}

	public void setCoverImageBase64(String coverImageBase64) {
		this.coverImageBase64 = coverImageBase64;
	}

	public byte[] getCoverImageBytes() {
		byte[] image = null;
		if (coverImageBase64 != null && !coverImageBase64.isEmpty())
			image = Base64.getDecoder().decode(coverImageBase64);
		return image;
	}

	public Map<String, String> toHashMap() {
		Map<String, String> map = new HashMap<>();
		map.put("title", title != null ? title : "");
		map.put("author", author != null ? author : "");
		map.put("language", language != null ? language : "");
		map.put("releaseDate", releaseDate != null ? new SimpleDateFormat("yyyy-MM-dd").format(releaseDate) : "");
		map.put("content", content != null ? content : "");
		map.put("price", String.valueOf(price));
		map.put("coverImageBytes", coverImageBase64 != null ? coverImageBase64 : "");
		return map;
	}

	public static Book fromHashMap(Map<String, String> map) {
		Book book = new Book();

		book.setTitle(map.getOrDefault("title", ""));
		book.setAuthor(map.getOrDefault("author", ""));
		book.setLanguage(map.getOrDefault("language", ""));

		String releaseDateStr = map.get("releaseDate");
		if (releaseDateStr != null && !releaseDateStr.isEmpty()) {
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				book.setReleaseDate(dateFormat.parse(releaseDateStr));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		book.setContent(map.getOrDefault("content", ""));
		book.setPrice(Integer.parseInt(map.getOrDefault("price", "0")));

		String coverImageBytesStr = map.get("coverImageBytes");
		if (coverImageBytesStr != null && !coverImageBytesStr.isEmpty()) {
			book.setCoverImageBase64(coverImageBytesStr);
		}

		return book;
	}

	@Override
	public int hashCode() {
		return Objects.hash(author, language, releaseDate, title);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Book other = (Book) obj;
		return Objects.equals(author, other.author) && Objects.equals(language, other.language)
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

}
