package org.unibl.etf.mdp.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** A supplier's invoice for one order: the books, the processing date and the price. */
public class Invoice implements Serializable {

    private static final long serialVersionUID = 2L;
    private LocalDate date;
    private List<InvoiceItem> items;
    private int totalPrice;
    private double VAT;

    public Invoice() {
        this.date = LocalDate.now();
        this.items = new ArrayList<>();
        this.totalPrice = 0;
    }

    public void addItem(BookDto book, int quantity, int unitPrice) {
        InvoiceItem item = new InvoiceItem(book.getTitle(), book.getAuthor(), quantity, unitPrice);
        items.add(item);
        totalPrice += item.getLineTotal();
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public double getVAT() {
        return VAT;
    }

    public void setVAT(double vAT) {
        VAT = vAT;
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, date, totalPrice);
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
        return Objects.equals(items, other.items) && Objects.equals(date, other.date) && totalPrice == other.totalPrice;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Invoice:\n");
        sb.append("Date: ").append(date).append("\n");
        sb.append("Books:\n");
        for (InvoiceItem item : items) {
            sb.append(" - ").append(item).append("\n");
        }
        sb.append("Total Price: ").append(totalPrice).append("\n");
        sb.append("VAT (17%): ").append(String.format(Locale.ROOT, "%.2f", VAT)).append("\n");
        return sb.toString();
    }

}
