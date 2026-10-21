package org.unibl.etf.mdp.common;

import com.google.gson.JsonParseException;
import org.unibl.etf.mdp.model.Message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * A socket that carries {@link Message}s as JSON, one message per line.
 * Lines longer than {@link #MAX_MESSAGE_BYTES} are refused, so a peer cannot exhaust memory.
 */
public class JsonChannel implements Closeable {
    // A Gutenberg book with its cover is a few MB; this leaves plenty of room
    public static final int MAX_MESSAGE_BYTES = 32 * 1024 * 1024;

    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;

    public JsonChannel(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedInputStream(socket.getInputStream());
        this.out = new BufferedOutputStream(socket.getOutputStream());
    }

    public static JsonChannel connect(String host, int port) throws IOException {
        return new JsonChannel(new Socket(host, port));
    }

    public synchronized void send(Message message) throws IOException {
        out.write(Json.toBytes(message));
        out.write('\n');
        out.flush();
    }

    /** Sends a request and waits for the answer. */
    public Message request(Message message) throws IOException {
        send(message);
        return receive();
    }

    /** @throws EOFException when the peer closed the connection */
    public Message receive() throws IOException {
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != '\n') {
            if (b == -1) {
                throw new EOFException("Connection closed");
            }
            if (line.size() >= MAX_MESSAGE_BYTES) {
                throw new IOException("Message larger than " + MAX_MESSAGE_BYTES + " bytes");
            }
            line.write(b);
        }
        try {
            Message message = Json.fromJson(line.toString(StandardCharsets.UTF_8), Message.class);
            if (message == null || message.getType() == null) {
                throw new IOException("Message without a type");
            }
            return message;
        } catch (JsonParseException e) {
            throw new IOException("Malformed message", e);
        }
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
