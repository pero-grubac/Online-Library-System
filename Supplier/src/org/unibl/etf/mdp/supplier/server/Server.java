package org.unibl.etf.mdp.supplier.server;

import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.properties.AppConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The supplier's own TCP server, where the library reads the offer. It is registered with the DiscoveryServer
 * under the supplier's name and kept registered with a heartbeat.
 */
public class Server {
    private static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(Server.class.getName());
    private final String discoveryHost;
    private final int discoveryPort;
    private final int heartbeatSeconds;
    private final String serverName;
    // Shared with the GUI: books added to the offer are served at once
    private final List<BookDto> books;
    private ServerSocket serverSocket;
    private ScheduledExecutorService heartbeat;
    private final AtomicBoolean running = new AtomicBoolean(false);

    /** Registration refused: another running application already uses this supplier's name. */
    public static class NameTakenException extends IOException {
        public NameTakenException(String name) {
            super(name + " is already running");
        }
    }

    public Server(String serverName, List<BookDto> books) {
        this(serverName, books, conf.getDefaultHost(), conf.getDiscoveryServerTCPPort(), conf.getHeartbeatSeconds());
    }

    Server(String serverName, List<BookDto> books, String discoveryHost, int discoveryPort, int heartbeatSeconds) {
        this.serverName = serverName;
        this.books = books;
        this.discoveryHost = discoveryHost;
        this.discoveryPort = discoveryPort;
        this.heartbeatSeconds = heartbeatSeconds;
    }

    /**
     * Opens the server on a free port and registers it.
     *
     * @throws NameTakenException when another application already runs as this supplier
     * @throws IOException        when the DiscoveryServer cannot be reached
     */
    public synchronized void start() throws IOException {
        serverSocket = new ServerSocket(0);
        try {
            String answer = sendDiscoveryMessage(new Message(conf.getDiscoveryMsg(), serverName, getServerPort()));
            if (conf.getTakenMsg().equals(answer)) {
                throw new NameTakenException(serverName);
            }
            if (!conf.getOkMsg().equals(answer)) {
                throw new IOException("Unexpected answer from the Discovery Server: " + answer);
            }
        } catch (IOException e) {
            serverSocket.close();
            throw e;
        }
        running.set(true);
        startHeartbeat();
        Thread acceptor = new Thread(this::acceptLoop, serverName + "-server");
        acceptor.setDaemon(true);
        acceptor.start();
        logger.info(serverName + " server running on port: " + getServerPort());
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket sock = serverSocket.accept();
                new ServerThread(sock, serverName, books);
            } catch (Exception e) {
                if (running.get()) {
                    logger.log(Level.SEVERE, "Error accepting connection", e);
                }
            }
        }
    }

    public int getServerPort() {
        return serverSocket.getLocalPort();
    }

    public synchronized void shutdown() {
        if (!running.getAndSet(false)) {
            return;
        }
        if (heartbeat != null) {
            heartbeat.shutdownNow();
        }
        try {
            // Leave the registry now instead of waiting for the TTL
            sendDiscoveryMessage(new Message(conf.getUnregisterMsg(), serverName, null, Message.ENTITY_SERVER));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not unregister from the Discovery Server", e);
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error during server shutdown", e);
        }
        logger.info(serverName + " server has been stopped.");
    }

    /** Re-registers periodically; the DiscoveryServer drops suppliers that stop doing so. */
    private void startHeartbeat() {
        heartbeat = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, serverName + "-heartbeat");
            t.setDaemon(true);
            return t;
        });
        Message renewal = new Message(conf.getDiscoveryMsg(), serverName, getServerPort());
        heartbeat.scheduleAtFixedRate(() -> {
            try {
                sendDiscoveryMessage(renewal);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Heartbeat to the Discovery Server failed", e);
            }
        }, heartbeatSeconds, heartbeatSeconds, TimeUnit.SECONDS);
    }

    /** @return the type of the answer */
    private String sendDiscoveryMessage(Message msg) throws IOException {
        try (JsonChannel channel = JsonChannel.connect(discoveryHost, discoveryPort)) {
            Message response = channel.request(msg);
            channel.send(new Message(conf.getEndMsg(), serverName));
            return response.getType();
        }
    }
}
