package com.microservices.notificationservice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public final class RoleUtil {

    private static final String ADMIN_ROLE = "admin";

    private RoleUtil() {
    }

    public static List<String> getRoles(Jwt jwt) {
        if (jwt == null) {
            return List.of();
        }
        List<String> allRoles = new ArrayList<>();

        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof Map<?, ?> realmAccessMap) {
            Object rolesObj = realmAccessMap.get("roles");
            if (rolesObj instanceof List<?> list) {
                for (Object o : list) {
                    if (o != null) {
                        allRoles.add(o.toString());
                    }
                }
            }
        }

        Object resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess instanceof Map<?, ?> resourceAccessMap) {
            Object clientAccess = resourceAccessMap.get("microservices-client");
            if (clientAccess instanceof Map<?, ?> clientAccessMap) {
                Object clientRolesObj = clientAccessMap.get("roles");
                if (clientRolesObj instanceof List<?> list) {
                    for (Object o : list) {
                        if (o != null) {
                            allRoles.add(o.toString());
                        }
                    }
                }
            }
        }

        if (!allRoles.isEmpty()) {
            return allRoles;
        }

        Object rolesClaim = jwt.getClaim("roles");
        if (rolesClaim instanceof List<?> list) {
            List<String> roles = new ArrayList<>();
            for (Object o : list) {
                if (o != null) {
                    roles.add(o.toString());
                }
            }
            return roles;
        }

        log.debug("No roles in JWT for news authorization");
        return List.of();
    }

    public static boolean isAdmin(Jwt jwt) {
        return getRoles(jwt).stream().anyMatch(r -> ADMIN_ROLE.equalsIgnoreCase(r));
    }
}
