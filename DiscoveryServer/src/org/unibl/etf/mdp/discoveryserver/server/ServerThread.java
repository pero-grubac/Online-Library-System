package org.unibl.etf.mdp.discoveryserver.server;

import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.discoveryserver.logger.FileLogger;
import org.unibl.etf.mdp.discoveryserver.properties.AppConfig;
import org.unibl.etf.mdp.model.Message;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.time.Clock;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThread extends Thread {
    public static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(ServerThread.class.getName());
    private static final String DISCOVER_SUPPLIER_MSG = conf.getDiscoveryMsg();
    private static final String END_MSG = conf.getEndMsg();
    private static final String OK_MSG = conf.getOkMsg();
    private static final String DISCOVER_ALL_SUPPLIERS_MSG = conf.getDiscoverAllMsg();
    private static final String DISCOVER_USER_MSG = conf.getDiscoverUserMsg();
    private static final String DISCOVER_ALL_USERS_MSG = conf.getDiscoverAllUsersMsg();
    private static final String UNREGISTER_MSG = conf.getUnregisterMsg();
    private static final String TAKEN_MSG = conf.getTakenMsg();
    private static final Duration TTL = Duration.ofSeconds(conf.getRegistrationTtlSeconds());
    // Shared by every connection thread
    private static final Registry servers = new Registry(TTL, Clock.systemUTC());
    private static final Registry users = new Registry(TTL, Clock.systemUTC());
    private final JsonChannel channel;

    public ServerThread(Socket sock) throws IOException {
        this.channel = new JsonChannel(sock);
        start();
    }

    public void run() {
        try {
            while (true) {
                Message request = channel.receive();
                if (DISCOVER_SUPPLIER_MSG.equals(request.getType()) || DISCOVER_USER_MSG.equals(request.getType())) {
                    boolean supplier = DISCOVER_SUPPLIER_MSG.equals(request.getType());
                    Integer port = parsePort(request);
                    if (request.getUsername() == null || port == null) {
                        logger.log(Level.WARNING, "Invalid registration: " + request);
                        break;
                    }
                    if ((supplier ? servers : users).register(request.getUsername(), port)) {
                        logger.info("Registered " + (supplier ? "server" : "user") + ": "
                                + request.getUsername() + " on port: " + port);
                        channel.send(new Message(OK_MSG));
                    } else {
                        channel.send(new Message(TAKEN_MSG));
                    }
                } else if (UNREGISTER_MSG.equals(request.getType())) {
                    Registry registry = Message.ENTITY_USER.equals(request.getEntityType()) ? users : servers;
                    registry.unregister(request.getUsername());
                    logger.info("Unregistered: " + request.getUsername());
                    channel.send(new Message(OK_MSG));
                } else if (DISCOVER_ALL_SUPPLIERS_MSG.equals(request.getType())) {
                    channel.send(new Message(DISCOVER_ALL_SUPPLIERS_MSG, "DiscoveryServer", servers.active()));
                } else if (DISCOVER_ALL_USERS_MSG.equals(request.getType())) {
                    channel.send(new Message(DISCOVER_ALL_USERS_MSG, "DiscoveryServer", users.active()));
                } else if (END_MSG.equals(request.getType())) {
                    break;
                } else {
                    logger.log(Level.SEVERE, "Unknown message type: " + request.getType());
                }
            }
        } catch (EOFException e) {
            // Client closed the connection without END
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Connection ended with an error.", ex);
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing resources.", e);
            }
        }
    }

    // The port may arrive as a number or as text
    private static Integer parsePort(Message request) {
        try {
            String port = request.getBody(String.class);
            return port == null ? null : Integer.valueOf(port.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
