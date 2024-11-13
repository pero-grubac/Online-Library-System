package org.unibl.etf.mdp.supplier.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.unibl.etf.mdp.library.model.Invoice;

public interface IAccountingService extends Remote {
	 double addInvoice(Invoice invoice) throws RemoteException;
}
