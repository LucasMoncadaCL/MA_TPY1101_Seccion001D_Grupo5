package com.panol_project.backendpanol.modules.users.application;

import com.panol_project.backendpanol.modules.auth.domain.AuditLogPort;
import com.panol_project.backendpanol.modules.users.application.dto.CreateUserCommand;
import com.panol_project.backendpanol.modules.users.application.dto.UpdateUserCommand;
import com.panol_project.backendpanol.modules.users.domain.UserAdminRepository;
import com.panol_project.backendpanol.modules.users.domain.UserAdminSummary;
import com.panol_project.backendpanol.shared.error.ApiException;
import com.panol_project.backendpanol.shared.outbox.application.OutboxService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdminService {

    private static final Set<String> ALLOWED_ROLES = Set.of("DIRECTOR", "COORDINADOR", "DOCENTE");

    private final UserAdminRepository repository;
    private final AuditLogPort auditLogPort;
    private final OutboxService outboxService;

    public UserAdminService(UserAdminRepository repository, AuditLogPort auditLogPort, OutboxService outboxService) {
        this.repository = repository;
        this.auditLogPort = auditLogPort;
        this.outboxService = outboxService;
    }

    @Transactional
    public void createUser(CreateUserCommand command, Jwt jwt) {
        String role = normalizeRole(command.role());
        String normalizedRut = normalizeRut(command.rut());
        String normalizedEmail = normalizeEmail(command.email());
        UUID roleUuid = repository.findRoleUuid(role);
        if (roleUuid == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ROLE_NOT_SUPPORTED", "Rol invalido");
        }

        if (repository.countUsersByRutOrEmail(normalizedRut, normalizedEmail) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "USER_DUPLICATED", "No fue posible procesar la solicitud");
        }

        repository.createUser(
                command.name().trim(),
                normalizedRut,
                normalizedEmail,
                BCrypt.hashpw(command.password(), BCrypt.gensalt()),
                roleUuid,
                true
        );

        auditLogPort.log("user_created", getUserUuid(jwt), null, Map.of("rut", normalizedRut, "email", normalizedEmail, "role", role));
        outboxService.enqueue("user", null, "UserCreated", getUserUuid(jwt), Map.of("rut", normalizedRut, "email", normalizedEmail, "role", role));
    }

    @Transactional
    public void changeRole(UUID userUuid, String roleInput, Jwt jwt) {
        String role = normalizeRole(roleInput);
        UUID roleUuid = repository.findRoleUuid(role);
        if (roleUuid == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ROLE_NOT_SUPPORTED", "Rol invalido");
        }
        int updated = repository.updateUserRole(userUuid, roleUuid);

        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado");
        }

        auditLogPort.log("user_role_changed", getUserUuid(jwt), userUuid, Map.of("new_role", role));
        outboxService.enqueue("user", userUuid, "UserRoleChanged", getUserUuid(jwt), Map.of("new_role", role));
    }

    @Transactional(readOnly = true)
    public List<UserAdminSummary> listUsers() {
        return repository.listUsers().stream()
                .map(row -> new UserAdminSummary(
                        row.uuid(),
                        row.name(),
                        row.rut(),
                        row.email(),
                        normalizeRoleForResponse(row.role()),
                        row.active(),
                        row.createdAt()))
                .toList();
    }

    @Transactional
    public void setActive(UUID userUuid, boolean active, Jwt jwt) {
        UUID actorUuid = getUserUuid(jwt);
        if (actorUuid != null && actorUuid.equals(userUuid) && !active) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "USER_SELF_DEACTIVATION_NOT_ALLOWED", "No puedes desactivar tu propio usuario");
        }

        int updated = repository.updateUserActive(userUuid, active);

        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado");
        }

        auditLogPort.log(
                active ? "user_activated" : "user_deactivated",
                actorUuid,
                userUuid,
                Map.of("active", active));
        outboxService.enqueue("user", userUuid, active ? "UserActivated" : "UserDeactivated", actorUuid, Map.of("active", active));
    }

    @Transactional
    public void updateUser(UUID userUuid, UpdateUserCommand command, Jwt jwt) {
        if (!existsUserByUuid(userUuid)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado");
        }
        UUID actorUuid = getUserUuid(jwt);
        String normalizedRut = normalizeRut(command.rut());
        String normalizedEmail = normalizeEmail(command.email());

        if (repository.countUsersByRutOrEmailExcludingUser(normalizedRut, normalizedEmail, userUuid) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "USER_DUPLICATED", "No fue posible procesar la solicitud");
        }

        int updated = repository.updateUser(userUuid, command.name().trim(), normalizedRut, normalizedEmail);

        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado");
        }

        auditLogPort.log("user_updated", actorUuid, userUuid, Map.of("rut", normalizedRut, "email", normalizedEmail));
        outboxService.enqueue("user", userUuid, "UserUpdated", actorUuid, Map.of("rut", normalizedRut, "email", normalizedEmail));
    }

    public void deleteUser(UUID userUuid, Jwt jwt) {
        setActive(userUuid, false, jwt);
    }

    private String normalizeRole(String roleRaw) {
        String role = roleRaw == null ? "" : roleRaw.trim().toUpperCase();
        if (role.contains("DIRECTOR")) role = "DIRECTOR";
        else if (role.contains("COORD")) role = "COORDINADOR";
        else if (role.contains("DOCENTE")) role = "DOCENTE";
        if (!ALLOWED_ROLES.contains(role)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ROLE_NOT_SUPPORTED", "Rol invalido");
        }
        return role;
    }

    private String normalizeRoleForResponse(String roleRaw) {
        if (roleRaw == null || roleRaw.isBlank()) {
            return "DOCENTE";
        }
        String role = roleRaw.trim().toUpperCase();
        if (role.contains("DIRECTOR")) return "DIRECTOR";
        if (role.contains("COORD")) return "COORDINADOR";
        if (role.contains("DOCENTE")) return "DOCENTE";
        return role;
    }

    private String normalizeRut(String rutRaw) {
        if (rutRaw == null) return "";
        return rutRaw.replaceAll("\\D", "").trim();
    }

    private String normalizeEmail(String emailRaw) {
        if (emailRaw == null || emailRaw.trim().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "USER_EMAIL_REQUIRED", "El correo es obligatorio");
        }
        return emailRaw.trim().toLowerCase();
    }

    private UUID getUserUuid(Jwt jwt) {
        if (jwt == null) return null;
        String subject = jwt.getSubject();
        if (subject != null && !subject.isBlank()) {
            UUID uuid = tryParseUuid(subject);
            if (uuid != null && existsUserByUuid(uuid)) {
                return uuid;
            }
        }
        return null;
    }

    private boolean existsUserByUuid(UUID uuid) {
        return repository.existsUserByUuid(uuid);
    }

    private UUID tryParseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

