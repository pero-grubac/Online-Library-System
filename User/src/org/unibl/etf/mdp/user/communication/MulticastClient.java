package org.unibl.etf.mdp.user.communication;

import java.util.logging.Logger;

import org.unibl.etf.mdp.user.logger.FileLogger;
import org.unibl.etf.mdp.user.properties.AppConfig;

public class MulticastClient {
	private static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(MulticastClient.class.getName());
	private String address;
	private int port;
	
	public MulticastClient(String address, int port) {
		super();
		this.address = address;
		this.port = port;
	}
	
}
