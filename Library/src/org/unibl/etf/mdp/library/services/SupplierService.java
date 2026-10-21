package org.unibl.etf.mdp.library.services;

import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.library.logger.FileLogger;
import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SupplierService {
    private static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(SupplierService.class.getName());
    private static final String GET_DTO_MSG = conf.getDtoMsg();
    private static final String END_MSG = conf.getEndMsg();
    private static final String OK_MSG = conf.getOkMsg();
    private static final String HOST = conf.getDefaultHost();

    public static List<BookDto> getOfferedBooks(String username, int port) {
        List<BookDto> books = new ArrayList<>();
        try (JsonChannel channel = JsonChannel.connect(HOST, port)) {
            channel.send(new Message(GET_DTO_MSG, username));
            while (true) {
                Message response = channel.receive();
                if (GET_DTO_MSG.equals(response.getType())) {
                    books.add(response.getBody(BookDto.class));
                    channel.send(new Message(OK_MSG, username));
                } else if (END_MSG.equals(response.getType())) {
                    break;
                } else {
                    logger.warning("Unknown message type received: " + response.getType());
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "An error occurred in the client application", ex);
        }
        return books;
    }
}
