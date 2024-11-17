package org.unibl.etf.mdp.library.observer;

import org.unibl.etf.mdp.library.model.Invoice;
import org.unibl.etf.mdp.library.event.Event;
import org.unibl.etf.mdp.library.event.InvoiceEvent;
import org.unibl.etf.mdp.library.interfaces.Observer;

public class InvoiceObserver implements Observer {
	@Override
	public void onEvent(Event event) {
		if (event instanceof InvoiceEvent) {
			Invoice invoice = ((InvoiceEvent) event).getInvoice();
		     handleInvoice(invoice);
        } else {
            System.err.println("Unexpected event type: " + event.getClass().getName());
        }
    }

    private void handleInvoice(Invoice invoice) {
        System.out.println("Invoice received: " + invoice);
        // Dodajte dodatnu logiku obrade ako je potrebno
    }
}
