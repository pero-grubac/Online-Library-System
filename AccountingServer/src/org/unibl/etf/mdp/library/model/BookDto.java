package org.unibl.etf.mdp.library.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

public class BookDto implements Serializable{
	private static final long serialVersionUID = 1L;
	private static final Random rand = new Random();
	private String title;
	private String author;
	private String editor;
	private String language;
	private Date releaseDate;
	private int price;

	public BookDto() {
		super();
	}

	public BookDto(String title, String author, String editor, String language, Date releaseDate, int price) {
		super();
		this.title = title;
		this.author = author;
		this.editor = editor;
		this.language = language;
		this.releaseDate = releaseDate;
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

	public String getEditor() {
		return editor;
	}

	public void setEditor(String editor) {
		this.editor = editor;
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

	@Override
	public int hashCode() {
		return Objects.hash(author, editor, language, price, releaseDate, title);
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
		return Objects.equals(author, other.author) && Objects.equals(editor, other.editor)
				&& Objects.equals(language, other.language) && price == other.price
				&& Objects.equals(releaseDate, other.releaseDate) && Objects.equals(title, other.title);
	}

	@Override
	public String toString() {
		return "BookDto [title=" + title + ", author=" + author + ", editor=" + editor + ", language=" + language
				+ ", releaseDate=" + releaseDate + ", price=" + price + "]";
	}
}
