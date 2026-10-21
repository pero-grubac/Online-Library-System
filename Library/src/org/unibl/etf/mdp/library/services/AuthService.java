package org.unibl.etf.mdp.library.services;

import org.unibl.etf.mdp.common.RestClient;
import org.unibl.etf.mdp.library.logger.FileLogger;
import org.unibl.etf.mdp.library.properties.AppConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Logs the Library application in to the LibraryServer with the staff account. */
public class AuthService {
    private static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(AuthService.class.getName());

    public static boolean login() {
        try {
            RestClient.Result result = RestClient.shared()
                    .login(conf.getServerUsersUrl() + "login", conf.getAdminUsername(), conf.getAdminPassword());
            if (!result.ok()) {
                logger.warning("Staff login refused with HTTP " + result.status());
            }
            return result.ok();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "LibraryServer is not reachable", e);
            return false;
        }
    }
}
