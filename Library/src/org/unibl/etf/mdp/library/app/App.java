package org.unibl.etf.mdp.library.app;

import org.unibl.etf.mdp.library.gui.Data;
import org.unibl.etf.mdp.library.gui.MainFrame;

public class App {

    public static void main(String[] args) {
        Data data = Data.getInstance();
        System.out.println("Library");
        new MainFrame().setVisible(true);
    }

}
