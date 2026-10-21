package org.unibl.etf.mdp.library.gui;

import org.unibl.etf.mdp.common.GroupBus;
import org.unibl.etf.mdp.common.GroupChatHistory;
import org.unibl.etf.mdp.common.ProposalBoard;
import org.unibl.etf.mdp.library.logger.FileLogger;
import org.unibl.etf.mdp.library.mq.DirectSender;
import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.library.server.Server;
import org.unibl.etf.mdp.library.services.BookService;
import org.unibl.etf.mdp.library.services.DeliveryService;
import org.unibl.etf.mdp.model.ChatMessage;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.model.OrderItem;
import org.unibl.etf.mdp.model.Proposal;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** State of the library application: its server for suppliers, the multicast group and the order queue. */
public class Data {
    private static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(Data.class.getName());
    private static Data instance = null;
    private final String username = conf.getLibraryName();
    private final DeliveryService deliveries = new DeliveryService(BookService.getInstance()::add);
    private final GroupChatHistory chat = new GroupChatHistory();
    private final ProposalBoard proposals = new ProposalBoard();
    private final GroupBus bus = new GroupBus(conf.getMulticastAdd(), conf.getChatUsersPort());
    private final Server server = new Server(deliveries);
    private DirectSender sender;

    private Data() {
    }

    /**
     * Starts the library's server and joins the multicast group.
     *
     * @throws IOException when the server port is taken, i.e. another library application is running
     */
    public static synchronized Data start() throws IOException {
        if (instance == null) {
            Data data = new Data();
            data.server.start();
            data.bus.on(conf.getGroupMessage(), m -> data.chat.addMessage(m.getBody(ChatMessage.class)));
            data.bus.on(conf.getProposalMsg(), m -> data.proposals.update(m.getBody(Proposal.class)));
            data.bus.on(conf.getProposalAnswerMsg(), m -> data.proposals.update(m.getBody(Proposal.class)));
            try {
                data.bus.start();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Cannot join the multicast group", e);
            }
            instance = data;
        }
        return instance;
    }

    public static synchronized Data getInstance() {
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public DeliveryService getDeliveries() {
        return deliveries;
    }

    public GroupChatHistory getChat() {
        return chat;
    }

    public ProposalBoard getProposals() {
        return proposals;
    }

    public void sendGroupMessage(String text) throws IOException {
        bus.send(new Message(conf.getGroupMessage(), username,
                new ChatMessage(username, text, java.time.LocalDateTime.now())));
    }

    /** Multicasts the librarians' decision to every member. */
    public void answerProposal(Proposal proposal, boolean accepted, String answer) throws IOException {
        bus.send(new Message(conf.getProposalAnswerMsg(), username, proposal.answered(accepted, answer, username)));
    }

    /** Puts the order in the supplier's queue; it waits there even while the supplier is offline. */
    public synchronized void sendOrder(String supplier, List<OrderItem> items) throws IOException {
        try {
            if (sender == null) {
                sender = DirectSender.getInstance();
            }
            sender.sendMessage(supplier, new Message(conf.getRequestMsg(), username, items));
        } catch (IOException e) {
            sender = null;
            throw e;
        } catch (Exception e) {
            sender = null;
            throw new IOException("RabbitMQ is not reachable: " + e.getMessage(), e);
        }
    }
}
