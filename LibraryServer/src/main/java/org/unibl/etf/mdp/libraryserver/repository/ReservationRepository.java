package org.unibl.etf.mdp.libraryserver.repository;

import org.unibl.etf.mdp.libraryserver.properties.AppConfig;
import org.unibl.etf.mdp.model.Reservation;
import org.unibl.etf.mdp.model.ReservationStatus;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Reservations in Redis: one hash per reservation ({@code reservation:<id>}), a counter for the ids and a set
 * of all ids, so listing never needs to scan the key space.
 */
public class ReservationRepository {
    private static final AppConfig conf = new AppConfig();
    private static final String PREFIX = conf.getReservationKeyPrefix();
    private static final String SEQUENCE = PREFIX + "seq";
    private static final String IDS = PREFIX + "ids";
    private static ReservationRepository instance;
    private final Supplier<JedisPool> pool;

    private ReservationRepository() {
        this.pool = RedisConnection::pool;
    }

    public ReservationRepository(JedisPool pool) {
        this.pool = () -> pool;
    }

    public static synchronized ReservationRepository getInstance() {
        if (instance == null) {
            instance = new ReservationRepository();
        }
        return instance;
    }

    public Reservation create(Reservation reservation) {
        try (Jedis jedis = pool.get().getResource()) {
            reservation.setId(jedis.incr(SEQUENCE));
            jedis.hset(PREFIX + reservation.getId(), toHash(reservation));
            jedis.sadd(IDS, String.valueOf(reservation.getId()));
            return reservation;
        }
    }

    public Reservation findById(long id) {
        try (Jedis jedis = pool.get().getResource()) {
            Map<String, String> hash = jedis.hgetAll(PREFIX + id);
            return hash.isEmpty() ? null : fromHash(hash);
        }
    }

    /** Oldest first. */
    public List<Reservation> findAll() {
        List<Reservation> reservations = new ArrayList<>();
        try (Jedis jedis = pool.get().getResource()) {
            Pipeline pipeline = jedis.pipelined();
            List<Response<Map<String, String>>> hashes = new ArrayList<>();
            for (String id : jedis.smembers(IDS)) {
                hashes.add(pipeline.hgetAll(PREFIX + id));
            }
            pipeline.sync();
            for (Response<Map<String, String>> hash : hashes) {
                if (!hash.get().isEmpty()) {
                    reservations.add(fromHash(hash.get()));
                }
            }
        }
        reservations.sort(Comparator.comparing(Reservation::getCreatedAt).thenComparing(Reservation::getId));
        return reservations;
    }

    public void updateStatus(long id, ReservationStatus status) {
        try (Jedis jedis = pool.get().getResource()) {
            jedis.hset(PREFIX + id, "status", status.name());
        }
    }

    private static Map<String, String> toHash(Reservation r) {
        Map<String, String> hash = new HashMap<>();
        hash.put("id", String.valueOf(r.getId()));
        hash.put("username", r.getUsername());
        hash.put("bookKey", r.getBookKey());
        hash.put("bookTitle", r.getBookTitle() == null ? "" : r.getBookTitle());
        hash.put("bookAuthor", r.getBookAuthor() == null ? "" : r.getBookAuthor());
        hash.put("createdAt", r.getCreatedAt().toString());
        hash.put("status", r.getStatus().name());
        return hash;
    }

    private static Reservation fromHash(Map<String, String> hash) {
        return new Reservation(Long.parseLong(hash.get("id")), hash.get("username"), hash.get("bookKey"),
                hash.get("bookTitle"), hash.get("bookAuthor"), LocalDateTime.parse(hash.get("createdAt")),
                ReservationStatus.valueOf(hash.get("status")));
    }
}
