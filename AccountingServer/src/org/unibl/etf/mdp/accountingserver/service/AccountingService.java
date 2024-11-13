package org.unibl.etf.mdp.accountingserver.service;

import java.rmi.RemoteException;

import org.unibl.etf.mdp.library.model.Invoice;

public class AccountingService implements IAccountingService {

	public AccountingService() throws RemoteException {
		super();
	}

	@Override
	public double addInvoice(Invoice invoice, String username) throws RemoteException {
		invoice.setVAT(invoice.getTotalPrice() * 0.17);
		return invoice.getVAT();
	}

}
