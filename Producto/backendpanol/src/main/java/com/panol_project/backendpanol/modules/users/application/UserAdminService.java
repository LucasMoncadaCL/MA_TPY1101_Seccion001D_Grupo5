package com.panol_project.backendpanol.modules.users.application;

import com.panol_project.backendpanol.modules.auth.application.AuditLogService;
import com.panol_project.backendpanol.modules.users.api.dto.CreateUserRequest;
import com.panol_project.backendpanol.modules.users.api.dto.UpdateUserRequest;
import com.panol_project.backendpanol.modules.users.api.dto.UserAdminSummaryResponse;
import com.panol_project.backendpanol.shared.error.ApiException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

@Service
public class UserAdminService {

    private static final Set<String> ALLOWED_ROLES = Set.of("DIRECTOR", "COORDINADOR", "DOCENTE");

    private final DSLContext dsl;
    private final AuditLogService auditLogService;

    public UserAdminService(DSLContext dsl, AuditLogService auditLogService) {
        this.dsl = dsl;
        this.auditLogService = auditLogService;
    }

    public void createUser(CreateUserRequest request, Jwt jwt) {
        String role = normalizeRole(request.role());
        String normalizedRut = normalizeRut(request.rut());
        String normalizedEmail = normalizeEmail(request.email());
        Integer roleId = findRoleId(role);
        if (roleId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ROLE_NOT_SUPPORTED", "Rol invalido");
        }

        var normalizedRutField = field(
                "replace(replace(replace({0}, '.', ''), '-', ''), ' ', '')",
                String.class,
                field(name("rut")));

        var duplicateCondition = normalizedRutField.eq(normalizedRut);
        if (normalizedEmail != null) {
            duplicateCondition = duplicateCondition.or(field(name("email")).eq(normalizedEmail));
        }

        Integer duplicated = dsl.selectCount().from(table(name("user")))
                .where(duplicateCondition)
                .fetchOne(0, Integer.class);

        if (duplicated != null && duplicated > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "USER_DUPLICATED", "No fue posible procesar la solicitud");
        }

        dsl.insertInto(table(name("user")))
                .columns(field(name("name")), field(name("rut")), field(name("email")), field(name("password_hash")), field(name("role_id")), field(name("active")))
                .values(
                        request.name().trim(),
                        normalizedRut,
                        normalizedEmail,
                        BCrypt.hashpw(request.password(), BCrypt.gensalt()),
                        roleId,
                        true)
                .execute();

        auditLogService.log("user_created", getUserId(jwt), null, Map.of("rut", normalizedRut, "email", normalizedEmail == null ? "" : normalizedEmail, "role", role));
    }

    public void changeRole(String userRef, String roleInput, Jwt jwt) {
        String role = normalizeRole(roleInput);
        Integer roleId = findRoleId(role);
        if (roleId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ROLE_NOT_SUPPORTED", "Rol invalido");
        }
        Integer userId = resolveUserId(userRef);

        int updated = dsl.update(table(name("user")))
                .set(field(name("role_id")), roleId)
                .where(field(name("id")).eq(userId))
                .execute();

        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado");
        }

        auditLogService.log("user_role_changed", getUserId(jwt), userId, Map.of("new_role", role));
    }

    public List<UserAdminSummaryResponse> listUsers() {
        return dsl.select(
                        field(name("user", "id"), Integer.class),
                        field(name("user", "uuid"), UUID.class),
                        field(name("user", "name"), String.class),
                        field(name("user", "rut"), String.class),
                        field(name("user", "email"), String.class),
                        field(name("role", "name"), String.class),
                        field(name("user", "active"), Boolean.class),
                        field(name("user", "created_at"), OffsetDateTime.class))
                .from(table(name("user")))
                .join(table(name("role"))).on(field(name("role", "id")).eq(field(name("user", "role_id"))))
                .orderBy(field(name("user", "id")).asc())
                .fetch(record -> new UserAdminSummaryResponse(
                        record.value1(),
                        record.value2() == null ? null : record.value2().toString(),
                        record.value3(),
                        record.value4(),
                        record.value5(),
                        normalizeRole(record.value6()),
                        Boolean.TRUE.equals(record.value7()),
                        record.value8()));
    }

    public void setActive(String userRef, boolean active, Jwt jwt) {
        Integer userId = resolveUserId(userRef);
        Integer actorId = getUserId(jwt);
        if (actorId != null && actorId.equals(userId) && !active) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "USER_SELF_DEACTIVATION_NOT_ALLOWED", "No puedes desactivar tu propio usuario");
        }

        int updated = dsl.update(table(name("user")))
                .set(field(name("active")), active)
                .where(field(name("id")).eq(userId))
                .execute();

        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado");
        }

        auditLogService.log(
                active ? "user_activated" : "user_deactivated",
                actorId,
                userId,
                Map.of("active", active));
    }

    public void updateUser(String userRef, UpdateUserRequest request, Jwt jwt) {
        Integer userId = resolveUserId(userRef);
        Integer actorId = getUserId(jwt);
        String normalizedRut = normalizeRut(request.rut());
        String normalizedEmail = normalizeEmail(request.email());

        var normalizedRutField = field(
                "replace(replace(replace({0}, '.', ''), '-', ''), ' ', '')",
                String.class,
                field(name("rut")));

        var duplicateCondition = normalizedRutField.eq(normalizedRut);
        if (normalizedEmail != null) {
            duplicateCondition = duplicateCondition.or(field(name("email")).eq(normalizedEmail));
        }

        Integer duplicated = dsl.selectCount()
                .from(table(name("user")))
                .where(duplicateCondition)
                .and(field(name("id")).ne(userId))
                .fetchOne(0, Integer.class);

        if (duplicated != null && duplicated > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "USER_DUPLICATED", "No fue posible procesar la solicitud");
        }

        int updated = dsl.update(table(name("user")))
                .set(field(name("name")), request.name().trim())
                .set(field(name("rut")), normalizedRut)
                .set(field(name("email")), normalizedEmail)
                .where(field(name("id")).eq(userId))
                .execute();

        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado");
        }

        auditLogService.log("user_updated", actorId, userId, Map.of("rut", normalizedRut, "email", normalizedEmail == null ? "" : normalizedEmail));
    }

    public void deleteUser(String userRef, Jwt jwt) {
        setActive(userRef, false, jwt);
    }

    private Integer findRoleId(String normalizedRole) {
        return dsl.select(field(name("id"), Integer.class))
                .from(table(name("role")))
                .where(field(name("name")).likeIgnoreCase('%' + roleKey(normalizedRole) + '%'))
                .fetchOne(0, Integer.class);
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

    private String roleKey(String normalizedRole) {
        return switch (normalizedRole) {
            case "COORDINADOR" -> "COORD";
            default -> normalizedRole;
        };
    }

    private String normalizeRut(String rutRaw) {
        if (rutRaw == null) return "";
        return rutRaw.replaceAll("\\D", "").trim();
    }

    private String normalizeEmail(String emailRaw) {
        if (emailRaw == null) return null;
        String normalized = emailRaw.trim().toLowerCase();
        return normalized.isBlank() ? null : normalized;
    }

    private Integer getUserId(Jwt jwt) {
        if (jwt == null) return null;
        Number userId = jwt.getClaim("user_id");
        return userId == null ? null : userId.intValue();
    }

    private Integer resolveUserId(String userRef) {
        if (userRef == null || userRef.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "USER_ID_INVALID", "Usuario no encontrado");
        }

        Integer legacyId = tryParseInteger(userRef);
        if (legacyId != null) {
            return legacyId;
        }

        UUID uuid = tryParseUuid(userRef);
        if (uuid == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "USER_ID_INVALID", "Usuario no encontrado");
        }

        Integer userId = dsl.select(field(name("id"), Integer.class))
                .from(table(name("user")))
                .where(field(name("uuid")).eq(uuid))
                .fetchOne(0, Integer.class);

        if (userId == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado");
        }
        return userId;
    }

    private Integer tryParseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private UUID tryParseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

