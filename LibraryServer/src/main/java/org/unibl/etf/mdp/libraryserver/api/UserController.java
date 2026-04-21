package org.unibl.etf.mdp.libraryserver.api;

import org.unibl.etf.mdp.libraryserver.service.UserService;
import org.unibl.etf.mdp.model.User;
import org.unibl.etf.mdp.model.UserDto;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/users")
public class UserController {
    private final UserService service;

    public UserController() {
        this.service = new UserService();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserDto> getAll() {
        List<UserDto> userDtos = service.getAll();
        return userDtos;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(User user) {
        UserDto createdUser = service.add(user);
        if (createdUser == null) {
            return Response.status(Response.Status.CONFLICT).entity("User already exists").build();
        }
        return Response.status(Response.Status.CREATED).entity(createdUser).build();
    }

    @PUT
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
    @Path("/{username}")
    public Response deleteUser(@PathParam("username") String username) {
        boolean deleted = service.delete(username);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
        }
        return Response.ok("User deleted successfully").build();
    }

    @PATCH
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
            return Response.status(Response.Status.BAD_REQUEST).entity("Error updating status").build();
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(User user) {
        try {
            boolean loggedIn = service.login(user.getUsername(), user.getPassword());
            if (loggedIn) {
                return Response.ok("Login successful").build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid username or password").build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error during login").build();
        }
    }
}
