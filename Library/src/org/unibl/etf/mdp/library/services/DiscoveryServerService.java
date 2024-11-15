package org.unibl.etf.mdp.library.services;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.library.logger.FileLogger;
import org.unibl.etf.mdp.library.model.Message;

public class DiscoveryServerService {
	private static final AppConfig conf = new AppConfig();
	private static final int SUPPLIER_SERVER_TCP_PORT = conf.getDiscoveryServerTCPPort();
	private static final Logger logger = FileLogger.getLogger(DiscoveryServerService.class.getName());
	private static final String HOST = conf.getDefaultHost();

	public static Map<String, String> getSuppliers() {
		Map<String, String> suppliers = new HashMap<>();
		String disMsg = conf.getDiscoverAllMsg();
		String endMsg = conf.getEndMsg();
		Message msg = new Message(disMsg);
		try {
			InetAddress addr = InetAddress.getByName(HOST);
			Socket sock = new Socket(addr, SUPPLIER_SERVER_TCP_PORT);

			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());

			out.writeObject(msg);
			out.flush();

			Message response = (Message) in.readObject();
			String serverList = (String) response.getBody();
			String[] entries = serverList.replace("{", "").replace("}", "").split(", ");
			for (String entry : entries) {
				String[] keyValue = entry.split("=");
				String serverName = keyValue[0].trim();
				String port = keyValue[1].trim();
				suppliers.put(serverName, port);
			}

			msg = new Message(endMsg);
			out.writeObject(msg);
			out.flush();

			in.close();
			out.close();
			sock.close();
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "An error occurred in the client application", ex);
		}

		return suppliers;
	}
}
