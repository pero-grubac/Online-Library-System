package org.unibl.etf.mdp.library.server;

import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.library.logger.FileLogger;
import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.library.services.DeliveryService;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Invoice;
import org.unibl.etf.mdp.model.Message;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** One supplier delivery: approved books and their invoice, or the books it refused, then END. */
public class ServerThread extends Thread {
    public static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(ServerThread.class.getName());
    private static final String APPROVE_MSG = conf.getApproveMsg();
    private static final String DENIAL_MSG = conf.getDenialMsg();
    private static final String END_MSG = conf.getEndMsg();
    private static final String INVOICE_MSG = conf.getInvoiceMsg();
    private final JsonChannel channel;
    private final DeliveryService deliveries;

    public ServerThread(Socket socket, DeliveryService deliveries) throws IOException {
        this.channel = new JsonChannel(socket);
        this.deliveries = deliveries;
    }

    public void run() {
        String supplier = null;
        List<String> refused = new ArrayList<>();
        try {
            while (true) {
                Message request = channel.receive();
                supplier = request.getUsername() != null ? request.getUsername() : supplier;
                if (APPROVE_MSG.equals(request.getType())) {
                    // Suppliers send one approved book per message
                    deliveries.bookArrived(supplier, request.getBody(Book.class));
                } else if (INVOICE_MSG.equals(request.getType())) {
                    deliveries.invoiceArrived(supplier, request.getBody(Invoice.class));
                } else if (DENIAL_MSG.equals(request.getType())) {
                    refused.add(request.getBody(BookDto.class).getTitle());
                } else if (END_MSG.equals(request.getType())) {
                    break;
                } else {
                    logger.log(Level.WARNING, "Unknown message type received: " + request.getType());
                }
            }
        } catch (EOFException ex) {
            logger.log(Level.INFO, "Supplier closed the connection.");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception occurred during run.", ex);
        } finally {
            if (!refused.isEmpty()) {
                deliveries.orderRefused(supplier, refused);
            }
            try {
                channel.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing resources.", e);
            }
        }
    }
}
