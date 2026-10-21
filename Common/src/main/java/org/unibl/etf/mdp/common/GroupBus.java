package org.unibl.etf.mdp.common;

import org.unibl.etf.mdp.model.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The multicast group shared by the library and all members (group chat, book proposals and the answers).
 * A sent message is also handed to the local handlers at once, so the sender sees it even when the network
 * does not loop multicast back; the handlers ignore the looped-back copy by its id.
 */
public class GroupBus {
    // Largest UDP payload
    public static final int MAX_DATAGRAM = 65507;
    private static final Logger logger = Logger.getLogger(GroupBus.class.getName());

    private final String address;
    private final int port;
    private final Map<String, Consumer<Message>> handlers = new ConcurrentHashMap<>();
    private volatile MulticastSocket socket;
    private Thread listener;

    public GroupBus(String address, int port) {
        this.address = address;
        this.port = port;
    }

    /** Messages of this type go to the handler; others are ignored. */
    public void on(String type, Consumer<Message> handler) {
        handlers.put(type, handler);
    }

    public synchronized void start() throws IOException {
        if (listener != null) {
            return;
        }
        socket = new MulticastSocket(port);
        socket.joinGroup(InetAddress.getByName(address));
        listener = new Thread(this::listen, "multicast-" + port);
        listener.setDaemon(true);
        listener.start();
    }

    public synchronized void stop() {
        if (socket != null) {
            socket.close();
        }
        listener = null;
    }

    public void send(Message message) throws IOException {
        byte[] bytes = Json.toBytes(message);
        if (bytes.length > MAX_DATAGRAM) {
            throw new IOException("Message too large for one datagram");
        }
        try (DatagramSocket out = new DatagramSocket()) {
            out.send(new DatagramPacket(bytes, bytes.length, InetAddress.getByName(address), port));
        }
        dispatch(message);
    }

    private void listen() {
        byte[] buffer = new byte[MAX_DATAGRAM];
        while (!socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                handle(packet.getData(), packet.getLength());
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    logger.log(Level.WARNING, "Error while receiving a multicast message", e);
                }
            }
        }
    }

    /** Decodes one datagram and hands it to its handler; malformed datagrams are dropped. */
    void handle(byte[] data, int length) {
        Message message;
        try {
            message = Json.messageFromBytes(data, length);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Discarding malformed multicast message", e);
            return;
        }
        if (message != null && message.getType() != null) {
            dispatch(message);
        }
    }

    private void dispatch(Message message) {
        Consumer<Message> handler = handlers.get(message.getType());
        if (handler == null) {
            return;
        }
        try {
            handler.accept(message);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error while handling " + message.getType(), e);
        }
    }
}
