package com.panol_project.backendpanol.modules.users.domain;

import java.util.List;
import java.util.UUID;

public interface UserAdminRepository {
    UUID findRoleUuid(String normalizedRole);
    int countUsersByRutOrEmail(String normalizedRut, String normalizedEmail);
    int countUsersByRutOrEmailExcludingUser(String normalizedRut, String normalizedEmail, UUID userUuid);
    void createUser(String name, String rut, String email, String passwordHash, UUID roleUuid, boolean active);
    int updateUserRole(UUID userUuid, UUID roleUuid);
    List<UserAdminSummary> listUsers();
    int updateUserActive(UUID userUuid, boolean active);
    boolean existsUserByUuid(UUID userUuid);
    int updateUser(UUID userUuid, String name, String rut, String email);
}
