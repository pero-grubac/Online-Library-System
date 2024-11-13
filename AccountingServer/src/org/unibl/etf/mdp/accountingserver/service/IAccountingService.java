package org.unibl.etf.mdp.accountingserver.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.unibl.etf.mdp.library.model.Invoice;

public interface IAccountingService extends Remote {
	 double addInvoice(Invoice invoice,String username) throws RemoteException;
}
