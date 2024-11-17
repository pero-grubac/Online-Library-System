package org.unibl.etf.mdp.library.event;

import org.unibl.etf.mdp.library.model.Invoice;

public class InvoiceEvent extends Event {
	private final Invoice invoice;

	public InvoiceEvent(Invoice invoice) {
		this.invoice = invoice;
	}

	public Invoice getInvoice() {
		return invoice;
	}
}
