package org.unibl.etf.mdp.library.server;

import java.net.Socket;

public class ServerThread {
	private Socket socket;

	public ServerThread(Socket socket) {
		this.socket = socket;
	}
}
