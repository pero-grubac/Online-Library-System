package org.unibl.etf.mdp.library.services;

import org.unibl.etf.mdp.common.Json;
import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.library.logger.FileLogger;
import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.model.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscoveryServerService {
    private static final AppConfig conf = new AppConfig();
    private static final int DISCOVERY_SERVER_TCP_PORT = conf.getDiscoveryServerTCPPort();
    private static final Logger logger = FileLogger.getLogger(DiscoveryServerService.class.getName());
    private static final String HOST = conf.getDefaultHost();

    /** Online suppliers and their ports; empty when the DiscoveryServer cannot be reached. */
    public static Map<String, String> getSuppliers() {
        try (JsonChannel channel = JsonChannel.connect(HOST, DISCOVERY_SERVER_TCP_PORT)) {
            Message response = channel.request(new Message(conf.getDiscoverAllMsg()));
            channel.send(new Message(conf.getEndMsg()));
            Map<String, String> suppliers = response.getBody(Json.STRING_MAP);
            return suppliers == null ? new HashMap<>() : new HashMap<>(suppliers);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "An error occurred in the client application", ex);
            return new HashMap<>();
        }
    }
}
