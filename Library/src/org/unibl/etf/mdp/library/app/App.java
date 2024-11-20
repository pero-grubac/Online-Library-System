package org.unibl.etf.mdp.library.app;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.unibl.etf.mdp.library.gui.Data;
import org.unibl.etf.mdp.library.gui.MainFrame;
import org.unibl.etf.mdp.library.mq.DirectSender;
import org.unibl.etf.mdp.library.observer.BookObserver;
import org.unibl.etf.mdp.library.observer.InvoiceObserver;
import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.library.server.Server;
import org.unibl.etf.mdp.library.services.BookService;
import org.unibl.etf.mdp.library.services.DiscoveryServerService;
import org.unibl.etf.mdp.library.services.InvoiceService;
import org.unibl.etf.mdp.library.services.SupplierService;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Message;

import jdk.internal.org.jline.terminal.TerminalBuilder.SystemOutput;

public class App {

	public static void main(String[] args) {
		Data data = Data.getInstance();
		System.out.println("Library");
		new MainFrame().setVisible(true);
	}

}
