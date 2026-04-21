package org.unibl.etf.mdp.service;

import org.unibl.etf.mdp.model.Invoice;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAccountingService extends Remote {
    Invoice addInvoice(Invoice invoice, String username) throws RemoteException;
}
