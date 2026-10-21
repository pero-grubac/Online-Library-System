package org.unibl.etf.mdp.libraryserver.security;

import org.unibl.etf.mdp.libraryserver.repository.UserRepository;
import org.unibl.etf.mdp.model.StatusEnum;

import javax.annotation.Priority;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;

/**
 * Every endpoint is closed unless it says otherwise: {@code @PermitAll} is public, {@code @RolesAllowed}
 * needs a valid Bearer token with one of the roles. No token or a bad one gives 401, a wrong role 403.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {
    private static final String BEARER = "Bearer ";

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext request) {
        Method method = resourceInfo.getResourceMethod();
        if (method == null || method.isAnnotationPresent(PermitAll.class)) {
            return;
        }
        RolesAllowed allowed = method.isAnnotationPresent(RolesAllowed.class)
                ? method.getAnnotation(RolesAllowed.class)
                : resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class);

        Optional<TokenService.Caller> caller = Optional.ofNullable(request.getHeaderString(HttpHeaders.AUTHORIZATION))
                .filter(h -> h.startsWith(BEARER))
                .flatMap(h -> TokenService.getInstance().verify(h.substring(BEARER.length()).trim()))
                .filter(AuthFilter::stillAllowed);
        if (caller.isEmpty()) {
            request.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer").entity("Login required").build());
            return;
        }
        if (allowed == null || Arrays.stream(allowed.value()).noneMatch(caller.get().getRole()::equals)) {
            request.abortWith(Response.status(Response.Status.FORBIDDEN).entity("Not allowed").build());
            return;
        }
        request.setSecurityContext(new CallerContext(caller.get(), request.getSecurityContext().isSecure()));
    }

    /** A member blocked or deleted after logging in loses access at once, not when the token expires. */
    private static boolean stillAllowed(TokenService.Caller caller) {
        if (!Roles.MEMBER.equals(caller.getRole())) {
            return true;
        }
        return UserRepository.getInstance().findByUsername(caller.getUsername())
                .map(u -> u.getStatus() == StatusEnum.APPROVED)
                .orElse(false);
    }

    private static final class CallerContext implements SecurityContext {
        private final TokenService.Caller caller;
        private final boolean secure;

        CallerContext(TokenService.Caller caller, boolean secure) {
            this.caller = caller;
            this.secure = secure;
        }

        @Override
        public Principal getUserPrincipal() {
            return caller::getUsername;
        }

        @Override
        public boolean isUserInRole(String role) {
            return caller.getRole().equals(role);
        }

        @Override
        public boolean isSecure() {
            return secure;
        }

        @Override
        public String getAuthenticationScheme() {
            return "Bearer";
        }
    }
}
