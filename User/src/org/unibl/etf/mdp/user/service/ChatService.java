package org.unibl.etf.mdp.user.service;

import org.unibl.etf.mdp.common.Json;
import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.model.ChatMessage;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.user.chat.ChatHistory;
import org.unibl.etf.mdp.user.logger.FileLogger;
import org.unibl.etf.mdp.user.properties.AppConfig;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Private chat between members: registration with the DiscoveryServer and sending over TLS. */
public class ChatService {
    private static final AppConfig conf = new AppConfig();
    private static final int DISCOVERY_SERVER_TCP_PORT = conf.getDiscoveryServerTCPPort();
    private static final Logger logger = FileLogger.getLogger(ChatService.class.getName());
    private static final String HOST = conf.getDefaultHost();
    private static final String DISCOVER_USER_MSG = conf.getDiscoverUserMsg();
    private static final String DISCOVER_ALL_USERS_MSG = conf.getDiscoverAllUsersMsg();
    private static final String UNREGISTER_MSG = conf.getUnregisterMsg();
    private static final String CHAT_MSG = conf.getChatMsg();
    private static final String END_MSG = conf.getEndMsg();
    private static final String OK_MSG = conf.getOk();
    private static final String TAKEN_MSG = conf.getTakenMsg();
    private static ChatService instance;
    private final ChatHistory chatHistory = ChatHistory.getInstance();
    private final String username;
    private volatile int port;
    private volatile String problem = "The chat is starting...";
    private ScheduledExecutorService heartbeat;

    private ChatService(String username) {
        this.username = username;
    }

    public static synchronized ChatService getInstance(String username) {
        if (instance == null)
            instance = new ChatService(username);
        return instance;
    }

    /** Null when others can reach this member; otherwise why not. */
    public String getProblem() {
        return problem;
    }

    /** Members that are online, with their chat ports. */
    public Map<String, String> getUsers() throws IOException {
        try (JsonChannel channel = JsonChannel.connect(HOST, DISCOVERY_SERVER_TCP_PORT)) {
            Message response = channel.request(new Message(DISCOVER_ALL_USERS_MSG, username));
            channel.send(new Message(END_MSG));
            Map<String, String> users = response.getBody(Json.STRING_MAP);
            return users == null ? new HashMap<>() : new HashMap<>(users);
        }
    }

    /**
     * Called once the chat server listens: registers its port and keeps the registration alive with a heartbeat
     * until the application exits.
     */
    public synchronized void start(int chatPort) {
        this.port = chatPort;
        register();
        if (heartbeat == null) {
            heartbeat = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "discovery-heartbeat");
                t.setDaemon(true);
                return t;
            });
            int every = conf.getHeartbeatSeconds();
            heartbeat.scheduleAtFixedRate(this::register, every, every, TimeUnit.SECONDS);
            Runtime.getRuntime().addShutdownHook(new Thread(this::unregister));
        }
    }

    private void register() {
        try {
            String answer = send(new Message(DISCOVER_USER_MSG, username, port));
            problem = OK_MSG.equals(answer) ? null
                    : TAKEN_MSG.equals(answer) ? username + " is already logged in elsewhere, so this window cannot receive messages."
                    : "Unexpected answer from the Discovery Server: " + answer;
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Could not reach the Discovery Server", ex);
            problem = "The Discovery Server is not reachable; others cannot see you in the chat.";
        }
    }

    public synchronized void unregister() {
        if (heartbeat != null) {
            heartbeat.shutdownNow();
        }
        try {
            send(new Message(UNREGISTER_MSG, username, null, Message.ENTITY_USER));
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Could not unregister from the Discovery Server", ex);
        }
    }

    private String send(Message message) throws IOException {
        try (JsonChannel channel = JsonChannel.connect(HOST, DISCOVERY_SERVER_TCP_PORT)) {
            Message response = channel.request(message);
            channel.send(new Message(END_MSG));
            return response.getType();
        }
    }

    /** @throws IOException when the message did not reach the recipient */
    public void sendMessage(String recipient, int recipientPort, String messageBody) throws IOException {
        try (JsonChannel channel = new JsonChannel(SSLSocketFactory.getDefault().createSocket(HOST, recipientPort))) {
            Message response = channel.request(new Message(CHAT_MSG, username, messageBody));
            if (!OK_MSG.equals(response.getType())) {
                throw new IOException(recipient + " did not accept the message");
            }
            chatHistory.addMessage(recipient, new ChatMessage(username, messageBody, LocalDateTime.now()));
        }
    }
}
