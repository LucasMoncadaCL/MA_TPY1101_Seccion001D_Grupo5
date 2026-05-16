package com.panol_project.backendpanol.modules.catalog.category.infrastructure;

import static com.panol_project.backendpanol.jooq.tables.Category.CATEGORY;
import static com.panol_project.backendpanol.jooq.tables.Implement.IMPLEMENT;
import static org.jooq.impl.DSL.lower;
import static org.jooq.impl.DSL.noCondition;

import com.panol_project.backendpanol.modules.catalog.category.domain.Categoria;
import com.panol_project.backendpanol.modules.catalog.category.domain.CategoriaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
public class CategoriaJooqRepository implements CategoriaRepository {

    private final DSLContext dsl;
    private static final Field<UUID> CATEGORY_UUID = DSL.field(DSL.name("category", "uuid"), UUID.class);
    private static final Field<String> CATEGORY_NAME = DSL.field(DSL.name("category", "name"), String.class);
    private static final Field<String> CATEGORY_DESCRIPTION = DSL.field(DSL.name("category", "description"), String.class);
    private static final Field<Boolean> CATEGORY_ACTIVE = DSL.field(DSL.name("category", "active"), Boolean.class);
    private static final Field<OffsetDateTime> CATEGORY_CREATED_AT = DSL.field(
            DSL.name("category", "created_at"),
            OffsetDateTime.class
    );
    private static final Field<UUID> IMPLEMENT_CATEGORY_UUID = DSL.field(DSL.name("implement", "category_uuid"), UUID.class);

    public CategoriaJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<Categoria> findAll(boolean includeInactive) {
        Condition condition = includeInactive ? noCondition() : CATEGORY_ACTIVE.isTrue();

        return dsl.select(CATEGORY_UUID, CATEGORY_NAME, CATEGORY_DESCRIPTION, CATEGORY_ACTIVE, CATEGORY_CREATED_AT)
                .from(CATEGORY)
                .where(condition)
                .orderBy(CATEGORY_NAME.asc())
                .fetch(this::toDomain);
    }

    @Override
    public Optional<Categoria> findByUuid(UUID uuid) {
        return dsl.select(CATEGORY_UUID, CATEGORY_NAME, CATEGORY_DESCRIPTION, CATEGORY_ACTIVE, CATEGORY_CREATED_AT)
                .from(CATEGORY)
                .where(CATEGORY_UUID.eq(uuid))
                .fetchOptional()
                .map(this::toDomain);
    }

    @Override
    public boolean existsByNombre(String nombre, UUID excludingUuid) {
        Condition condition = lower(CATEGORY_NAME).eq(nombre.toLowerCase(Locale.ROOT));
        if (excludingUuid != null) {
            condition = condition.and(CATEGORY_UUID.ne(excludingUuid));
        }

        return dsl.fetchExists(dsl.selectOne().from(CATEGORY).where(condition));
    }

    @Override
    public Optional<Categoria> findActiveByUuid(UUID uuid) {
        return dsl.select(CATEGORY_UUID, CATEGORY_NAME, CATEGORY_DESCRIPTION, CATEGORY_ACTIVE, CATEGORY_CREATED_AT)
                .from(CATEGORY)
                .where(CATEGORY_UUID.eq(uuid).and(CATEGORY_ACTIVE.isTrue()))
                .fetchOptional()
                .map(this::toDomain);
    }

    @Override
    public Categoria create(String nombre, String descripcion) {
        return dsl.insertInto(CATEGORY)
                .set(CATEGORY_NAME, nombre)
                .set(CATEGORY_DESCRIPTION, descripcion)
                .set(CATEGORY_ACTIVE, true)
                .set(CATEGORY_CREATED_AT, OffsetDateTime.now())
                .returningResult(CATEGORY_UUID, CATEGORY_NAME, CATEGORY_DESCRIPTION, CATEGORY_ACTIVE, CATEGORY_CREATED_AT)
                .fetchOptional()
                .map(this::toDomain)
                .orElseThrow();
    }

    @Override
    public Categoria updateNombre(UUID uuid, String nombre, String descripcion) {
        return dsl.update(CATEGORY)
                .set(CATEGORY_NAME, nombre)
                .set(CATEGORY_DESCRIPTION, descripcion)
                .where(CATEGORY_UUID.eq(uuid))
                .returningResult(CATEGORY_UUID, CATEGORY_NAME, CATEGORY_DESCRIPTION, CATEGORY_ACTIVE, CATEGORY_CREATED_AT)
                .fetchOptional()
                .map(this::toDomain)
                .orElseThrow();
    }

    @Override
    public void deactivate(UUID uuid) {
        dsl.update(CATEGORY)
                .set(CATEGORY_ACTIVE, false)
                .where(CATEGORY_UUID.eq(uuid))
                .execute();
    }

    @Override
    public void deleteByUuid(UUID uuid) {
        dsl.deleteFrom(CATEGORY)
                .where(CATEGORY_UUID.eq(uuid))
                .execute();
    }

    @Override
    public int countImplementsByCategoryUuid(UUID categoryUuid) {
        return dsl.fetchCount(
                dsl.selectOne()
                        .from(IMPLEMENT)
                        .where(IMPLEMENT_CATEGORY_UUID.eq(categoryUuid))
        );
    }

    @Override
    public int countActiveImplementsByCategoryUuid(UUID categoryUuid) {
        return dsl.fetchCount(
                dsl.selectOne()
                        .from(IMPLEMENT)
                        .where(IMPLEMENT_CATEGORY_UUID.eq(categoryUuid)
                                .and(IMPLEMENT.ACTIVE.isTrue()))
        );
    }

    private Categoria toDomain(Record record) {
        return new Categoria(
                record.get(CATEGORY_UUID),
                record.get(CATEGORY_NAME),
                record.get(CATEGORY_DESCRIPTION),
                record.get(CATEGORY_ACTIVE),
                record.get(CATEGORY_CREATED_AT)
        );
    }
}
