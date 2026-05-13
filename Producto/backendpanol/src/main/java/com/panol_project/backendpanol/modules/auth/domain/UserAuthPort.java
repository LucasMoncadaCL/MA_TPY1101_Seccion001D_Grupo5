package com.panol_project.backendpanol.modules.auth.domain;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserAuthPort {
    Optional<AuthUser> findAuthUserByRut(String rut);
    void registerFailedAttempt(UUID userUuid, int attempts, OffsetDateTime blockedUntil);
    void resetLoginAttempts(UUID userUuid, OffsetDateTime lastLoginAt);
}
