package org.unibl.etf.mdp.user.service;

import org.unibl.etf.mdp.common.ApiException;
import org.unibl.etf.mdp.common.Json;
import org.unibl.etf.mdp.common.RestClient;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Reservation;
import org.unibl.etf.mdp.user.properties.AppConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/** The member's own book reservations. */
public class ReservationService {
    private static final AppConfig conf = new AppConfig();
    private static final String RESERVATIONS_URL = conf.getReservationsUrl();
    private static ReservationService instance;

    private ReservationService() {
    }

    public static synchronized ReservationService getInstance() {
        if (instance == null) {
            instance = new ReservationService();
        }
        return instance;
    }

    public Reservation reserve(BookDto book) throws IOException {
        String body = Json.toJson(Map.of("bookKey", book.getKey()));
        return Json.fromJson(ApiException.check(RestClient.shared().post(RESERVATIONS_URL, body)).body(),
                Reservation.class);
    }

    public List<Reservation> mine() throws IOException {
        return Json.fromJson(ApiException.check(RestClient.shared().get(RESERVATIONS_URL + "mine")).body(),
                Json.listOf(Reservation.class));
    }
}
