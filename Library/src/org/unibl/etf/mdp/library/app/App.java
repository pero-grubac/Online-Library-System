package org.unibl.etf.mdp.library.app;

import org.unibl.etf.mdp.common.ui.Theme;
import org.unibl.etf.mdp.library.gui.Data;
import org.unibl.etf.mdp.library.gui.MainFrame;
import org.unibl.etf.mdp.library.services.AuthService;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;

public class App {

    public static void main(String[] args) {
        Theme.apply();
        Data data;
        try {
            data = Data.start();
        } catch (IOException e) {
            // Only one library application may run
            JOptionPane.showMessageDialog(null, "The library application is already running (" + e.getMessage() + ").",
                    "Library", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }
        if (!AuthService.login()) {
            JOptionPane.showMessageDialog(null,
                    "Could not log in to the LibraryServer as library staff.\n"
                            + "Check that it is running and that LIBRARY_ADMIN_PASSWORD matches its ADMIN_PASSWORD.",
                    "Library", JOptionPane.WARNING_MESSAGE);
        }
        SwingUtilities.invokeLater(() -> new MainFrame(data).setVisible(true));
    }
}
