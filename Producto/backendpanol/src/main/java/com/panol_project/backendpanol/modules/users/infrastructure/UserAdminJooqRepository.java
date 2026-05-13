package com.panol_project.backendpanol.modules.users.infrastructure;

import com.panol_project.backendpanol.modules.users.domain.UserAdminRepository;
import com.panol_project.backendpanol.modules.users.domain.UserAdminSummary;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

@Repository
public class UserAdminJooqRepository implements UserAdminRepository {

    private final DSLContext dsl;

    public UserAdminJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public UUID findRoleUuid(String normalizedRole) {
        return dsl.select(field(name("uuid"), UUID.class))
                .from(table(name("role")))
                .where(field(name("name")).likeIgnoreCase('%' + roleKey(normalizedRole) + '%'))
                .fetchOne(0, UUID.class);
    }

    @Override
    public int countUsersByRutOrEmail(String normalizedRut, String normalizedEmail) {
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
        return duplicated == null ? 0 : duplicated;
    }

    @Override
    public int countUsersByRutOrEmailExcludingUser(String normalizedRut, String normalizedEmail, UUID userUuid) {
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
                .and(field(name("uuid"), UUID.class).ne(userUuid))
                .fetchOne(0, Integer.class);
        return duplicated == null ? 0 : duplicated;
    }

    @Override
    public void createUser(String name, String rut, String email, String passwordHash, UUID roleUuid, boolean active) {
        dsl.insertInto(table(name("user")))
                .columns(field(name("name")), field(name("rut")), field(name("email")), field(name("password_hash")), field(name("role_uuid")), field(name("active")))
                .values(name, rut, email, passwordHash, roleUuid, active)
                .execute();
    }

    @Override
    public int updateUserRole(UUID userUuid, UUID roleUuid) {
        return dsl.update(table(name("user")))
                .set(field(name("role_uuid"), UUID.class), roleUuid)
                .where(field(name("uuid"), UUID.class).eq(userUuid))
                .execute();
    }

    @Override
    public List<UserAdminSummary> listUsers() {
        return dsl.select(
                        field(name("user", "uuid"), UUID.class),
                        field(name("user", "name"), String.class),
                        field(name("user", "rut"), String.class),
                        field(name("user", "email"), String.class),
                        field(name("role", "name"), String.class),
                        field(name("user", "active"), Boolean.class),
                        field(name("user", "created_at"), OffsetDateTime.class))
                .from(table(name("user")))
                .join(table(name("role"))).on(field(name("role", "uuid")).eq(field(name("user", "role_uuid"))))
                .orderBy(field(name("user", "created_at")).desc())
                .fetch(record -> new UserAdminSummary(
                        record.value1() == null ? null : record.value1().toString(),
                        record.value2(),
                        record.value3(),
                        record.value4(),
                        record.value5(),
                        Boolean.TRUE.equals(record.value6()),
                        record.value7()));
    }

    @Override
    public int updateUserActive(UUID userUuid, boolean active) {
        return dsl.update(table(name("user")))
                .set(field(name("active")), active)
                .where(field(name("uuid"), UUID.class).eq(userUuid))
                .execute();
    }

    @Override
    public boolean existsUserByUuid(UUID userUuid) {
        Integer count = dsl.selectCount()
                .from(table(name("user")))
                .where(field(name("uuid")).eq(userUuid))
                .fetchOne(0, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public int updateUser(UUID userUuid, String name, String rut, String email) {
        return dsl.update(table(name("user")))
                .set(field(name("name")), name)
                .set(field(name("rut")), rut)
                .set(field(name("email")), email)
                .where(field(name("uuid"), UUID.class).eq(userUuid))
                .execute();
    }

    private String roleKey(String normalizedRole) {
        return switch (normalizedRole) {
            case "COORDINADOR" -> "COORD";
            default -> normalizedRole;
        };
    }
}
