package com.panol_project.backendpanol.modules.users.application;

import com.panol_project.backendpanol.modules.auth.application.AuditLogService;
import com.panol_project.backendpanol.modules.users.api.dto.CreateUserRequest;
import com.panol_project.backendpanol.shared.error.ApiException;
import java.util.Map;
import java.util.Set;
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
        Integer roleId = findRoleId(role);
        if (roleId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ROLE_NOT_SUPPORTED", "Rol invalido");
        }

        Integer duplicated = dsl.selectCount().from(table(name("user")))
                .where(field(name("rut")).eq(request.rut().trim())
                        .or(field(name("email")).eq(request.email().trim().toLowerCase())))
                .fetchOne(0, Integer.class);

        if (duplicated != null && duplicated > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "USER_DUPLICATED", "No fue posible procesar la solicitud");
        }

        dsl.insertInto(table(name("user")))
                .columns(field(name("name")), field(name("rut")), field(name("email")), field(name("password_hash")), field(name("role_id")), field(name("active")))
                .values(
                        request.name().trim(),
                        request.rut().trim(),
                        request.email().trim().toLowerCase(),
                        BCrypt.hashpw(request.password(), BCrypt.gensalt()),
                        roleId,
                        true)
                .execute();

        auditLogService.log("user_created", getUserId(jwt), null, Map.of("rut", request.rut().trim(), "email", request.email().trim(), "role", role));
    }

    public void changeRole(Integer userId, String roleInput, Jwt jwt) {
        String role = normalizeRole(roleInput);
        Integer roleId = findRoleId(role);
        if (roleId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ROLE_NOT_SUPPORTED", "Rol invalido");
        }

        int updated = dsl.update(table(name("user")))
                .set(field(name("role_id")), roleId)
                .where(field(name("id")).eq(userId))
                .execute();

        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Usuario no encontrado");
        }

        auditLogService.log("user_role_changed", getUserId(jwt), userId, Map.of("new_role", role));
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

    private Integer getUserId(Jwt jwt) {
        if (jwt == null) return null;
        Number userId = jwt.getClaim("user_id");
        return userId == null ? null : userId.intValue();
    }
}

