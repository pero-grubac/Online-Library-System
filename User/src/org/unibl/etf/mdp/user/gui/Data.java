package org.unibl.etf.mdp.user.gui;

import org.unibl.etf.mdp.common.GroupBus;
import org.unibl.etf.mdp.common.GroupChatHistory;
import org.unibl.etf.mdp.common.ProposalBoard;
import org.unibl.etf.mdp.model.ChatMessage;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.model.Proposal;
import org.unibl.etf.mdp.user.chat.ChatServer;
import org.unibl.etf.mdp.user.logger.FileLogger;
import org.unibl.etf.mdp.user.properties.AppConfig;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/** State of the logged-in member: the private chat server and the multicast group. */
public class Data {
    private static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(Data.class.getName());
    private static Data instance;
    private final String username;
    private final GroupChatHistory groupChat = new GroupChatHistory();
    private final ProposalBoard proposals = new ProposalBoard();
    private final GroupBus bus = new GroupBus(conf.getMulticastAdd(), conf.getChatUsersPort());

    private Data(String name) {
        this.username = name;
        Thread chatServer = new Thread(ChatServer.getInstance(username), "chat-server");
        chatServer.setDaemon(true);
        chatServer.start();
        bus.on(conf.getGroupMessage(), m -> groupChat.addMessage(m.getBody(ChatMessage.class)));
        bus.on(conf.getProposalMsg(), m -> proposals.update(m.getBody(Proposal.class)));
        bus.on(conf.getProposalAnswerMsg(), m -> proposals.update(m.getBody(Proposal.class)));
        try {
            bus.start();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot join the multicast group", e);
        }
    }

    public static synchronized Data getInstance(String name) {
        if (instance == null)
            instance = new Data(name);
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public GroupChatHistory getGroupChat() {
        return groupChat;
    }

    public ProposalBoard getProposals() {
        return proposals;
    }

    public void sendGroupMessage(String text) throws IOException {
        bus.send(new Message(conf.getGroupMessage(), username, new ChatMessage(username, text, LocalDateTime.now())));
    }

    /** Multicasts a proposal to the librarians and to every member. */
    public void propose(String title, String author, String note) throws IOException {
        bus.send(new Message(conf.getProposalMsg(), username, new Proposal(username, title, author, note)));
    }
}
