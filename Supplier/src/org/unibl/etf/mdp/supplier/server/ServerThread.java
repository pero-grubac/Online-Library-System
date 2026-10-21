package org.unibl.etf.mdp.supplier.server;

import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.properties.AppConfig;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThread extends Thread {
    public static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(ServerThread.class.getName());
    private static final String END_MSG = conf.getEndMsg();
    private static final String GET_DTO_MSG = conf.getDtoMsg();
    private static final String OK_MSG = conf.getOkMsg();
    private final JsonChannel channel;
    private String serverName;
    private List<BookDto> books;

    public ServerThread(Socket sock, String serverName, List<BookDto> books) throws IOException {
        this.channel = new JsonChannel(sock);
        this.serverName = serverName;
        this.books = books;
        start();
    }

    public void run() {
        try {
            while (true) {
                Message request = channel.receive();
                if (GET_DTO_MSG.equals(request.getType())) {
                    for (BookDto book : books) {
                        Message acknowledgment = channel.request(new Message(GET_DTO_MSG, serverName, book));
                        if (!OK_MSG.equals(acknowledgment.getType())) {
                            logger.warning("Did not receive OK acknowledgment for book: " + book);
                            break;
                        }
                    }
                    channel.send(new Message(END_MSG, serverName));
                    break;
                } else if (END_MSG.equals(request.getType())) {
                    break;
                } else {
                    logger.log(Level.WARNING, "Unknown message type received: " + request.getType());
                }
            }
        } catch (EOFException ex) {
            logger.log(Level.INFO, "Client ended the connection.");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception occurred during run.", ex);
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        try {
            channel.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error closing resources.", e);
        }
    }
}
