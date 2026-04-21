package org.unibl.etf.mdp.supplier.app;

import org.unibl.etf.mdp.service.IAccountingService;
import org.unibl.etf.mdp.supplier.gui.LoginFrame;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.properties.AppConfig;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    public static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(App.class.getName());
    private static IAccountingService accountingService;

    public static void main(String[] args) {

        System.out.println("Supplier client");
        initializeRMI();
        new LoginFrame().setVisible(true);

    }

    public static void initializeRMI() {
        try {
            String securityDir = conf.getSecurityDir();
            String securityFile = conf.getSecurityFile();
            String securityPolicy = conf.getSecurityPolicy();
            System.setProperty(securityPolicy, securityDir + File.separator + securityFile);
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
            String registryName = conf.getRegistryName();
            int registryPort = conf.getRegistryPort();
            Registry registry = LocateRegistry.getRegistry(registryPort);

            accountingService = (IAccountingService) registry.lookup(registryName);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize RMI", e);
        }
    }

    public static IAccountingService getAccountingService() {
        return accountingService;
    }
}
