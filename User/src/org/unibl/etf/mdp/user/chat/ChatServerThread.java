package org.unibl.etf.mdp.user.chat;

import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.model.ChatMessage;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.user.logger.FileLogger;
import org.unibl.etf.mdp.user.properties.AppConfig;

import javax.net.ssl.SSLSocket;
import java.io.EOFException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServerThread extends Thread {
    public static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(ChatServerThread.class.getName());
    private static final String CHAT_MESSAGE = conf.getChatMsg();
    private static final String OK_MSG = conf.getOk();
    private final JsonChannel channel;
    private ChatHistory chatHistory;

    public ChatServerThread(SSLSocket socket) throws IOException {
        this.channel = new JsonChannel(socket);
        chatHistory = ChatHistory.getInstance();
        start();
    }

    public void run() {
        try {
            while (true) {
                Message request = channel.receive();
                if (CHAT_MESSAGE.equals(request.getType())) {
                    String user = request.getUsername();
                    String text = request.getBody(String.class);
                    ChatMessage cm = new ChatMessage(user, text, LocalDateTime.now());
                    chatHistory.addMessage(user, cm);
                    channel.send(new Message(OK_MSG));
                    break;
                } else {
                    logger.log(Level.WARNING, "Unknown message type received: " + request.getType());
                }
            }
        } catch (EOFException e) {
            logger.log(Level.INFO, "Client disconnected (EOF).");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception occurred during run.", ex);
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing resources.", e);
            }
        }
    }
}
