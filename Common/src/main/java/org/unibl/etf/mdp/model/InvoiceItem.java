package org.unibl.etf.mdp.model;

import java.io.Serializable;
import java.util.Objects;

/** A book on an invoice, with the unit price set for this order. */
public class InvoiceItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title;
    private String author;
    private int quantity;
    private int unitPrice;

    public InvoiceItem() {
    }

    public InvoiceItem(String title, String author, int quantity, int unitPrice) {
        this.title = title;
        this.author = author;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public int getLineTotal() {
        return quantity * unitPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof InvoiceItem))
            return false;
        InvoiceItem other = (InvoiceItem) o;
        return quantity == other.quantity && unitPrice == other.unitPrice && Objects.equals(title, other.title)
                && Objects.equals(author, other.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, author, quantity, unitPrice);
    }

    @Override
    public String toString() {
        return author + " - " + title + ": " + quantity + " x " + unitPrice + " = " + getLineTotal();
    }
}
