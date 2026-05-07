package com.panol_project.backendpanol.modules.auth.infrastructure;

import java.time.OffsetDateTime;

public interface TokenRevocationRepository {
    void revokeToken(String jti, Integer userId, OffsetDateTime expiresAt);
    boolean isRevoked(String jti);
}

