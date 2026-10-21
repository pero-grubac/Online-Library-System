package org.unibl.etf.mdp.common;

import java.io.ObjectInputFilter;
import java.util.logging.Logger;

/**
 * RMI is the one place that still uses Java serialization. This JEP 290 filter lets through only the system's
 * own model, the JDK and RMI's internal classes, and rejects everything else before it is instantiated.
 */
public final class SerialFilter {
    public static final String PATTERN = "maxdepth=20;maxrefs=100000;"
            + "org.unibl.etf.mdp.model.*;org.unibl.etf.mdp.service.*;java.base/*;java.rmi/*;"
            // RMI stubs are dynamic proxies; their handler and interfaces are checked by this same filter
            + "jdk.proxy*;!*";
    private static final Logger logger = Logger.getLogger(SerialFilter.class.getName());

    private SerialFilter() {
    }

    /** Installs the process-wide filter; safe to call more than once. */
    public static synchronized void install() {
        if (ObjectInputFilter.Config.getSerialFilter() == null) {
            ObjectInputFilter.Config.setSerialFilter(create());
        }
    }

    static ObjectInputFilter create() {
        ObjectInputFilter allowList = ObjectInputFilter.Config.createFilter(PATTERN);
        return info -> {
            ObjectInputFilter.Status status = allowList.checkInput(info);
            if (status == ObjectInputFilter.Status.REJECTED) {
                logger.warning("Rejected during deserialization: " + info.serialClass());
            }
            return status;
        };
    }
}
