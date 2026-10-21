package org.unibl.etf.mdp.supplier.app;

import org.unibl.etf.mdp.common.SerialFilter;
import org.unibl.etf.mdp.common.ui.Theme;
import org.unibl.etf.mdp.service.IAccountingService;
import org.unibl.etf.mdp.supplier.gui.LoginFrame;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.properties.AppConfig;

import javax.swing.SwingUtilities;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    public static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(App.class.getName());
    private static IAccountingService accountingService;

    public static void main(String[] args) {
        // Only the system's own classes may be deserialized from RMI responses
        SerialFilter.install();
        initializeRMI();
        Theme.apply();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    /** @return false when the AccountingServer cannot be reached; approving orders then has to wait for it */
    public static synchronized boolean initializeRMI() {
        try {
            Registry registry = LocateRegistry.getRegistry(conf.getRmiHost(), conf.getRegistryPort());
            accountingService = (IAccountingService) registry.lookup(conf.getRegistryName());
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize RMI", e);
            accountingService = null;
            return false;
        }
    }

    /** Connects on demand, so an AccountingServer started later is picked up. */
    public static synchronized IAccountingService getAccountingService() {
        if (accountingService == null) {
            initializeRMI();
        }
        return accountingService;
    }

    /** Drops a stale stub after the AccountingServer was restarted. */
    public static synchronized void resetAccountingService() {
        accountingService = null;
    }
}
