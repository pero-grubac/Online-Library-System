package org.unibl.etf.mdp.library.services;

import org.unibl.etf.mdp.common.ApiException;
import org.unibl.etf.mdp.common.Json;
import org.unibl.etf.mdp.common.RestClient;
import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.model.Reservation;
import org.unibl.etf.mdp.model.ReservationStatus;

import java.io.IOException;
import java.util.List;

/** The members' book reservations, which the library confirms or rejects. */
public class ReservationService {
    private static final AppConfig conf = new AppConfig();
    private static final String RESERVATIONS_URL = conf.getReservationsUrl();
    private static ReservationService instance;
    private final RestClient api = RestClient.shared();

    private ReservationService() {
    }

    public static synchronized ReservationService getInstance() {
        if (instance == null) {
            instance = new ReservationService();
        }
        return instance;
    }

    /** All reservations, oldest first. */
    public List<Reservation> getAll() throws IOException {
        return Json.fromJson(ApiException.check(api.get(RESERVATIONS_URL)).body(), Json.listOf(Reservation.class));
    }

    public void decide(Reservation reservation, ReservationStatus decision) throws IOException {
        ApiException.check(api.patch(RESERVATIONS_URL + reservation.getId() + "?status=" + decision, "text/plain", ""));
    }
}
