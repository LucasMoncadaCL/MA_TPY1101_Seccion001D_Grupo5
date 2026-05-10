package com.panol_project.backendpanol.modules.auth.infrastructure;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuthUserRow(
        Integer id,
        UUID uuid,
        String rut,
        String passwordHash,
        String roleName,
        Integer failedLoginAttempts,
        OffsetDateTime blockedUntil
) {
}

