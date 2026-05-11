package com.panol_project.backendpanol.modules.catalog.category.infrastructure;

import static com.panol_project.backendpanol.jooq.tables.Category.CATEGORY;
import static com.panol_project.backendpanol.jooq.tables.Implement.IMPLEMENT;
import static org.jooq.impl.DSL.lower;
import static org.jooq.impl.DSL.noCondition;

import com.panol_project.backendpanol.jooq.tables.records.CategoryRecord;
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
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
public class CategoriaJooqRepository implements CategoriaRepository {

    private final DSLContext dsl;
    private static final Field<UUID> CATEGORY_UUID = DSL.field(DSL.name("uuid"), UUID.class);
    private static final Field<UUID> IMPLEMENT_CATEGORY_UUID = DSL.field(DSL.name("category_uuid"), UUID.class);

    public CategoriaJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<Categoria> findAll(boolean includeInactive) {
        Condition condition = includeInactive ? noCondition() : CATEGORY.ACTIVE.isTrue();

        return dsl.selectFrom(CATEGORY)
                .where(condition)
                .orderBy(CATEGORY.NAME.asc())
                .fetch(this::toDomain);
    }

    @Override
    public Optional<Categoria> findByUuid(UUID uuid) {
        return dsl.selectFrom(CATEGORY)
                .where(CATEGORY_UUID.eq(uuid))
                .fetchOptional()
                .map(this::toDomain);
    }

    @Override
    public boolean existsByNombre(String nombre, UUID excludingUuid) {
        Condition condition = lower(CATEGORY.NAME).eq(nombre.toLowerCase(Locale.ROOT));
        if (excludingUuid != null) {
            condition = condition.and(CATEGORY_UUID.ne(excludingUuid));
        }

        return dsl.fetchExists(dsl.selectOne().from(CATEGORY).where(condition));
    }

    @Override
    public Optional<Categoria> findActiveByUuid(UUID uuid) {
        return dsl.selectFrom(CATEGORY)
                .where(CATEGORY_UUID.eq(uuid).and(CATEGORY.ACTIVE.isTrue()))
                .fetchOptional()
                .map(this::toDomain);
    }

    @Override
    public Categoria create(String nombre, String descripcion) {
        return dsl.insertInto(CATEGORY)
                .set(CATEGORY.NAME, nombre)
                .set(CATEGORY.DESCRIPTION, descripcion)
                .set(CATEGORY.ACTIVE, true)
                .set(CATEGORY.CREATED_AT, OffsetDateTime.now())
                .returning()
                .fetchOptional()
                .map(this::toDomain)
                .orElseThrow();
    }

    @Override
    public Categoria updateNombre(UUID uuid, String nombre, String descripcion) {
        return dsl.update(CATEGORY)
                .set(CATEGORY.NAME, nombre)
                .set(CATEGORY.DESCRIPTION, descripcion)
                .where(CATEGORY_UUID.eq(uuid))
                .returning()
                .fetchOptional()
                .map(this::toDomain)
                .orElseThrow();
    }

    @Override
    public void deactivate(UUID uuid) {
        dsl.update(CATEGORY)
                .set(CATEGORY.ACTIVE, false)
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

    private Categoria toDomain(CategoryRecord record) {
        return new Categoria(
                record.get(CATEGORY_UUID),
                record.getName(),
                record.getDescription(),
                record.getActive(),
                record.getCreatedAt()
        );
    }
}
