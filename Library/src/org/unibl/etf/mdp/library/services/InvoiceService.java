package org.unibl.etf.mdp.library.services;

import java.util.ArrayList;
import java.util.List;

import org.unibl.etf.mdp.library.event.Event;
import org.unibl.etf.mdp.library.event.InvoiceEvent;
import org.unibl.etf.mdp.library.interfaces.Observer;
import org.unibl.etf.mdp.library.model.Invoice;
import org.unibl.etf.mdp.library.observer.InvoiceObserver;

public class InvoiceService {
	private static InvoiceService instance;
	private final List<InvoiceObserver> observers = new ArrayList<>();

	private InvoiceService() {

	}

	public static synchronized InvoiceService getInstance() {
		if (instance == null) {
			instance = new InvoiceService();
		}
		return instance;
	}

	public void addObserver(InvoiceObserver observer) {
		observers.add(observer);
	}

	public void removeObserver(InvoiceObserver observer) {
		observers.remove(observer);
	}

	public void notifyInvoiceReceived(Invoice invoice) {
		InvoiceEvent event = new InvoiceEvent(invoice);
		notifyObservers(event);
	}

	private void notifyObservers(Event event) {
		for (InvoiceObserver observer : observers) {
			observer.onEvent(event);
		}
	}
}
