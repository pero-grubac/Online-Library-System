package org.unibl.etf.mdp.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.unibl.etf.mdp.model.Invoice;

public interface IAccountingService extends Remote {
	 Invoice addInvoice(Invoice invoice,String username) throws RemoteException;
}
