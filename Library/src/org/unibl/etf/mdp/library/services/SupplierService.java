package org.unibl.etf.mdp.library.services;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.library.logger.FileLogger;
import org.unibl.etf.mdp.library.model.BookDto;
import org.unibl.etf.mdp.library.model.Message;
import org.unibl.etf.mdp.library.properties.AppConfig;

public class SupplierService {
	private static final AppConfig conf = new AppConfig();
	private static final int SUPPLIER_SERVER_TCP_PORT = conf.getDiscoveryServerTCPPort();
	private static final Logger logger = FileLogger.getLogger(DiscoveryServerService.class.getName());

	
	public List<BookDto> getOfferedBooks(String username){
		List<BookDto> books = new ArrayList<>();
		String dtoMsg = conf.getDtoMsg();
		String endMsg = conf.getEndMsg();
		Message request = new Message(dtoMsg,username);
		try {
			InetAddress addr = InetAddress.getByName("localhost");
			Socket sock = new Socket(addr, SUPPLIER_SERVER_TCP_PORT);

			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());

			out.writeObject(request);
			out.flush();
			
			
			
			request = new Message(endMsg);
			out.writeObject(request);
			out.flush();

			in.close();
			out.close();
			sock.close();
		}catch (Exception ex) {
			logger.log(Level.SEVERE, "An error occurred in the client application", ex);
		}
		
		return books;
	}
}
