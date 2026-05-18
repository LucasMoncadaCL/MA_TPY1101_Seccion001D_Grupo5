package com.panol_project.backendpanol.shared.persistence.rls;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import org.jooq.ExecuteContext;
import org.jooq.impl.DefaultExecuteListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AppCurrentUserRlsExecuteListener extends DefaultExecuteListener {

    private static final Logger LOG = LoggerFactory.getLogger(AppCurrentUserRlsExecuteListener.class);
    private static final String SET_RLS_USER_SQL = "select set_config('app.current_user_uuid', ?, false)";

    @Override
    public void executeStart(ExecuteContext ctx) {
        Connection connection = ctx.connection();
        if (connection == null) {
            return;
        }

        String currentUserUuid = resolveCurrentUserUuid();
        String rlsValue = currentUserUuid == null ? "" : currentUserUuid;

        try (PreparedStatement stmt = connection.prepareStatement(SET_RLS_USER_SQL)) {
            stmt.setString(1, rlsValue);
            stmt.execute();
        } catch (SQLException ex) {
            LOG.warn("rls_set_current_user_uuid_failed", ex);
        }
    }

    private String resolveCurrentUserUuid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return normalizeUuid(jwt.getSubject());
        }
        if (principal instanceof String value) {
            return normalizeUuid(value);
        }
        return null;
    }

    private String normalizeUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value).toString();
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
