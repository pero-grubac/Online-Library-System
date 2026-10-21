package org.unibl.etf.mdp.libraryserver.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.unibl.etf.mdp.common.Json;
import org.unibl.etf.mdp.libraryserver.logger.FileLogger;
import org.unibl.etf.mdp.libraryserver.security.Roles;
import org.unibl.etf.mdp.libraryserver.service.ReservationService;
import org.unibl.etf.mdp.libraryserver.service.ReservationService.ReservationException;
import org.unibl.etf.mdp.model.ReservationStatus;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Book reservations: members create them, the library reviews them. */
@Path("/reservations")
@Produces(MediaType.APPLICATION_JSON)
public class ReservationController {
    private static final Logger logger = FileLogger.getLogger(ReservationController.class.getName());
    private final ReservationService service = new ReservationService();

    private static Response json(Response.Status status, Object body) {
        return Response.status(status).entity(Json.toJson(body)).type(MediaType.APPLICATION_JSON).build();
    }

    private static Response run(Callable<Response> action) {
        try {
            return action.call();
        } catch (ReservationException e) {
            return Response.status(e.getStatus()).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Reservation request failed", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error processing the reservation")
                    .type(MediaType.TEXT_PLAIN).build();
        }
    }

    /** Body: {"bookKey": "..."} */
    @POST
    @RolesAllowed(Roles.MEMBER)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reserve(String body, @Context SecurityContext caller) {
        String bookKey;
        try {
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            bookKey = json.has("bookKey") ? json.get("bookKey").getAsString() : null;
        } catch (JsonParseException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid JSON format.").build();
        }
        return run(() -> json(Response.Status.CREATED,
                service.reserve(caller.getUserPrincipal().getName(), bookKey)));
    }

    @GET
    @RolesAllowed(Roles.ADMIN)
    public Response all() {
        return run(() -> json(Response.Status.OK, service.all()));
    }

    @GET
    @Path("/mine")
    @RolesAllowed(Roles.MEMBER)
    public Response mine(@Context SecurityContext caller) {
        return run(() -> json(Response.Status.OK, service.forMember(caller.getUserPrincipal().getName())));
    }

    @PATCH
    @Path("/{id}")
    @RolesAllowed(Roles.ADMIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public Response decide(@PathParam("id") long id, @QueryParam("status") String status) {
        ReservationStatus decision;
        try {
            decision = ReservationStatus.valueOf(String.valueOf(status).toUpperCase());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Status must be APPROVED or REJECTED").build();
        }
        return run(() -> json(Response.Status.OK, service.decide(id, decision)));
    }
}
