package org.unibl.etf.mdp.library.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.unibl.etf.mdp.library.model.Invoice;

public interface IAccountingService extends Remote {
	Invoice addInvoice(Invoice invoice, String username) throws RemoteException;
}
