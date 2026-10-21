package org.unibl.etf.mdp.library.server;

import org.unibl.etf.mdp.library.logger.FileLogger;
import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.library.services.DeliveryService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/** The library's TCP server, where suppliers deliver approved books, invoices and rejections. */
public class Server {
    private static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(Server.class.getName());
    private final int port;
    private final DeliveryService deliveries;
    private ServerSocket serverSocket;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public Server(DeliveryService deliveries) {
        this(conf.getTCPPort(), deliveries);
    }

    public Server(int port, DeliveryService deliveries) {
        this.port = port;
        this.deliveries = deliveries;
    }

    /**
     * Opens the fixed port. Only one library may run, so a second one fails here.
     *
     * @throws IOException when the port is taken
     */
    public synchronized void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running.set(true);
        Thread acceptor = new Thread(this::acceptLoop, "library-server");
        acceptor.setDaemon(true);
        acceptor.start();
        logger.info("Library is running on port " + serverSocket.getLocalPort());
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket sock = serverSocket.accept();
                new ServerThread(sock, deliveries).start();
            } catch (Exception e) {
                if (running.get()) {
                    logger.log(Level.SEVERE, "Error accepting connection", e);
                }
            }
        }
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public synchronized void shutdown() {
        running.set(false);
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error during server shutdown", e);
        }
    }
}
