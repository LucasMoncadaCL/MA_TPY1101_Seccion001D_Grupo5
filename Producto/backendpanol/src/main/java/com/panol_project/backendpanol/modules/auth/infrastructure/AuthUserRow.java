package com.panol_project.backendpanol.modules.auth.infrastructure;

import java.time.OffsetDateTime;

public record AuthUserRow(
        Integer id,
        String rut,
        String passwordHash,
        String roleName,
        Integer failedLoginAttempts,
        OffsetDateTime blockedUntil
) {
}

