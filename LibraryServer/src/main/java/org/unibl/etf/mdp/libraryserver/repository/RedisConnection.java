package org.unibl.etf.mdp.libraryserver.repository;

import org.unibl.etf.mdp.libraryserver.logger.FileLogger;
import org.unibl.etf.mdp.libraryserver.properties.AppConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.logging.Logger;

/** The one Redis connection pool of the server, shared by the book and reservation repositories. */
public final class RedisConnection {
    private static final Logger logger = FileLogger.getLogger(RedisConnection.class.getName());
    private static volatile JedisPool pool;

    private RedisConnection() {
    }

    public static JedisPool pool() {
        JedisPool current = pool;
        if (current == null) {
            synchronized (RedisConnection.class) {
                if (pool == null) {
                    AppConfig conf = new AppConfig();
                    logger.info("Connecting to Redis at " + conf.getRedisHost() + ":" + conf.getRedisPort());
                    pool = new JedisPool(new JedisPoolConfig(), conf.getRedisHost(), conf.getRedisPort());
                }
                current = pool;
            }
        }
        return current;
    }

    /** Points the whole server at another Redis, e.g. a test container. */
    public static synchronized void use(JedisPool other) {
        pool = other;
    }
}
