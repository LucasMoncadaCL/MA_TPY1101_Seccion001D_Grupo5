package com.panol_project.backendpanol.modules.auth.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuthUser(
        UUID uuid,
        String rut,
        String passwordHash,
        String roleName,
        Integer failedLoginAttempts,
        OffsetDateTime blockedUntil
) {
}
