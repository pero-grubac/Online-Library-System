package org.unibl.etf.mdp.libraryserver.api;

import org.unibl.etf.mdp.libraryserver.logger.FileLogger;
import org.unibl.etf.mdp.libraryserver.security.Roles;
import org.unibl.etf.mdp.libraryserver.security.TokenService;
import org.unibl.etf.mdp.libraryserver.service.UserService;
import org.unibl.etf.mdp.model.StatusEnum;
import org.unibl.etf.mdp.model.User;
import org.unibl.etf.mdp.model.UserDto;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/users")
public class UserController {
    private static final Logger logger = FileLogger.getLogger(UserController.class.getName());
    private final UserService service;

    public UserController() {
        this.service = new UserService();
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    @GET
    @RolesAllowed(Roles.ADMIN)
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserDto> getAll() {
        List<UserDto> userDtos = service.getAll();
        return userDtos;
    }

    @POST
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(User user) {
        if (user == null || blank(user.getUsername()) || blank(user.getPassword()) || blank(user.getEmail())
                || blank(user.getFirstName()) || blank(user.getLastName()) || blank(user.getAddress())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("First name, last name, address, e-mail, username and password are required").build();
        }
        UserDto createdUser = service.add(user);
        if (createdUser == null) {
            return Response.status(Response.Status.CONFLICT).entity("User already exists").build();
        }
        return Response.status(Response.Status.CREATED).entity(createdUser).build();
    }

    @PUT
    @RolesAllowed(Roles.ADMIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(UserDto userDto) {
        boolean updated = service.update(userDto);
        if (!updated) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Failed to update user").build();
        }
        return Response.ok("User updated successfully").build();
    }

    @DELETE
    @RolesAllowed(Roles.ADMIN)
    @Path("/{username}")
    public Response deleteUser(@PathParam("username") String username) {
        boolean deleted = service.delete(username);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
        }
        return Response.ok("User deleted successfully").build();
    }

    @PATCH
    @RolesAllowed(Roles.ADMIN)
    @Path("/{username}/status")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response changeUserStatus(@PathParam("username") String username, @QueryParam("status") String status) {
        try {
            boolean statusChanged = false;

            if ("APPROVED".equalsIgnoreCase(status)) {
                statusChanged = service.approver(username);
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                statusChanged = service.reject(username);
            } else if ("BLOCKED".equalsIgnoreCase(status)) {
                statusChanged = service.block(username);
            }

            if (!statusChanged) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid status or user not found").build();
            }
            return Response.ok("User status updated to " + status).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while changing the status of " + username, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error updating status").build();
        }
    }

    /** Approved members and the library staff account; answers with a Bearer token. */
    @POST
    @PermitAll
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(User user) {
        try {
            if (user == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing credentials").build();
            }
            Optional<String> role = service.authenticate(user.getUsername(), user.getPassword());
            if (role.isEmpty()) {
                // Right password, but the library has not approved the account (or blocked it)
                Optional<StatusEnum> status = service.accountStatus(user.getUsername(), user.getPassword());
                if (status.isPresent()) {
                    return Response.status(Response.Status.FORBIDDEN).type(MediaType.TEXT_PLAIN)
                            .entity("Your account is " + status.get() + ". The library has to approve it first.")
                            .build();
                }
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid username or password").build();
            }
            TokenService tokens = TokenService.getInstance();
            Map<String, Object> session = new LinkedHashMap<>();
            session.put("token", tokens.issue(user.getUsername(), role.get()));
            session.put("tokenType", "Bearer");
            session.put("expiresIn", tokens.ttlSeconds());
            session.put("username", user.getUsername());
            session.put("role", role.get());
            return Response.ok(session).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during login", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error during login").build();
        }
    }
}
