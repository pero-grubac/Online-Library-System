package org.unibl.etf.mdp.accountingserver.app;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.accountingserver.logger.FileLogger;
import org.unibl.etf.mdp.accountingserver.properties.AppConfig;
import org.unibl.etf.mdp.library.service.AccountingService;
import org.unibl.etf.mdp.library.service.IAccountingService;


public class App {
	private static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(App.class.getName());

	public static void main(String[] args) {
		String securityDir = conf.getSecurityDir();
		String securityFile = conf.getSecurityFile();
		String securityPolicy = conf.getSecurityPolicy();
		System.setProperty(securityPolicy, securityDir + File.separator + securityFile);
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {
			AccountingService service = new AccountingService();
			IAccountingService stub = (IAccountingService) UnicastRemoteObject.exportObject(service, 0);
			int registryPort = conf.getRegistryPort();
			Registry registry = LocateRegistry.createRegistry(registryPort);
			String registryName = conf.getRegistryName();
			registry.rebind(registryName, stub);
			System.out.println("Accounting server started.");
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "An error occurred in the server application", ex);
		}
	}

}
