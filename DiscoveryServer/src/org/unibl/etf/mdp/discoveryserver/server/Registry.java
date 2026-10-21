package org.unibl.etf.mdp.discoveryserver.server;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Who is online and on which port. Clients renew their registration with a heartbeat;
 * an entry that is not renewed within the TTL is treated as gone, so crashed clients disappear by themselves.
 */
public class Registry {
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();
    private final Duration ttl;
    private final Clock clock;

    public Registry(Duration ttl, Clock clock) {
        this.ttl = ttl;
        this.clock = clock;
    }

    /**
     * Registers or renews a name. A name that is still alive on another port belongs to a running client,
     * so a second client with the same name is refused.
     *
     * @return false when the name is taken
     */
    public boolean register(String name, int port) {
        Instant now = clock.instant();
        Entry fresh = new Entry(port, now);
        Entry result = entries.merge(name, fresh,
                (old, renewal) -> old.port == port || old.lastSeen.isBefore(now.minus(ttl)) ? renewal : old);
        return result == fresh;
    }

    public boolean unregister(String name) {
        return entries.remove(name) != null;
    }

    /** Live entries, sorted by name. */
    public Map<String, Integer> active() {
        Instant oldest = clock.instant().minus(ttl);
        entries.values().removeIf(e -> e.lastSeen.isBefore(oldest));
        Map<String, Integer> result = new TreeMap<>();
        entries.forEach((name, e) -> result.put(name, e.port));
        return result;
    }

    private static final class Entry {
        final int port;
        final Instant lastSeen;

        Entry(int port, Instant lastSeen) {
            this.port = port;
            this.lastSeen = lastSeen;
        }
    }
}
