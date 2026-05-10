package com.panol_project.backendpanol.modules.auth.infrastructure;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface TokenRevocationRepository {
    void revokeToken(String jti, Integer userId, UUID userUuid, OffsetDateTime expiresAt);
    boolean isRevoked(String jti);
}

