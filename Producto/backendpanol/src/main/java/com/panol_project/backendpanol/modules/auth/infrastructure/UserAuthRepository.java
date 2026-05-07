package com.panol_project.backendpanol.modules.auth.infrastructure;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface UserAuthRepository {
    Optional<AuthUserRow> findAuthUserByRut(String rut);
    void registerFailedAttempt(Integer userId, int attempts, OffsetDateTime blockedUntil);
    void resetLoginAttempts(Integer userId, OffsetDateTime lastLoginAt);
}

