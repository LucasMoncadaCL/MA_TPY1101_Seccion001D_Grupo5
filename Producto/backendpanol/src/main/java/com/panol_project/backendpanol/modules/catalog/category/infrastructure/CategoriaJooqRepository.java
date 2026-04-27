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
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class CategoriaJooqRepository implements CategoriaRepository {

    private final DSLContext dsl;

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
    public Optional<Categoria> findById(Integer id) {
        return dsl.selectFrom(CATEGORY)
                .where(CATEGORY.ID.eq(id))
                .fetchOptional()
                .map(this::toDomain);
    }

    @Override
    public boolean existsByNombre(String nombre, Integer excludingId) {
        Condition condition = lower(CATEGORY.NAME).eq(nombre.toLowerCase(Locale.ROOT));
        if (excludingId != null) {
            condition = condition.and(CATEGORY.ID.ne(excludingId));
        }

        return dsl.fetchExists(dsl.selectOne().from(CATEGORY).where(condition));
    }

    @Override
    public Optional<Categoria> findActiveById(Integer id) {
        return dsl.selectFrom(CATEGORY)
                .where(CATEGORY.ID.eq(id).and(CATEGORY.ACTIVE.isTrue()))
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
    public Categoria updateNombre(Integer id, String nombre, String descripcion) {
        return dsl.update(CATEGORY)
                .set(CATEGORY.NAME, nombre)
                .set(CATEGORY.DESCRIPTION, descripcion)
                .where(CATEGORY.ID.eq(id))
                .returning()
                .fetchOptional()
                .map(this::toDomain)
                .orElseThrow();
    }

    @Override
    public void deactivate(Integer id) {
        dsl.update(CATEGORY)
                .set(CATEGORY.ACTIVE, false)
                .where(CATEGORY.ID.eq(id))
                .execute();
    }

    @Override
    public void deleteById(Integer id) {
        dsl.deleteFrom(CATEGORY)
                .where(CATEGORY.ID.eq(id))
                .execute();
    }

    @Override
    public int countImplementsByCategoryId(Integer categoryId) {
        return dsl.fetchCount(
                dsl.selectOne()
                        .from(IMPLEMENT)
                        .where(IMPLEMENT.CATEGORY_ID.eq(categoryId))
        );
    }

    @Override
    public int countActiveImplementsByCategoryId(Integer categoryId) {
        return dsl.fetchCount(
                dsl.selectOne()
                        .from(IMPLEMENT)
                        .where(IMPLEMENT.CATEGORY_ID.eq(categoryId)
                                .and(IMPLEMENT.ACTIVE.isTrue()))
        );
    }

    private Categoria toDomain(CategoryRecord record) {
        return new Categoria(
                record.getId(),
                record.getName(),
                record.getDescription(),
                record.getActive(),
                record.getCreatedAt()
        );
    }
}
