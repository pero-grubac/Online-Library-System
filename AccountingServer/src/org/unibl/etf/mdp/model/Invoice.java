package org.unibl.etf.mdp.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Invoice implements Serializable {


	private static final long serialVersionUID = 1L;
	private LocalDate date;
	private List<BookDto> books;
	private int totalPrice;
	private double VAT;

	public Invoice() {
		this.date = LocalDate.now();
		this.books = new ArrayList<>();
		this.totalPrice = 0;
	}

	public void addBook(BookDto book) {
		books.add(book);
		totalPrice += book.getPrice();
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public List<BookDto> getBooks() {
		return books;
	}

	public void setBooks(List<BookDto> books) {
		this.books = books;
	}

	public int getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(int totalPrice) {
		this.totalPrice = totalPrice;
	}

	public double getVAT() {
		return VAT;
	}

	public void setVAT(double vAT) {
		VAT = vAT;
	}

	@Override
	public int hashCode() {
		return Objects.hash(books, date, totalPrice);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Invoice other = (Invoice) obj;
		return Objects.equals(books, other.books) && Objects.equals(date, other.date) && totalPrice == other.totalPrice;
	}

	@Override
	public String toString() {
		return "Invoice [date=" + date + ", books=" + books + ", totalPrice=" + totalPrice + ", VAT=" + VAT + "]";
	}

}
