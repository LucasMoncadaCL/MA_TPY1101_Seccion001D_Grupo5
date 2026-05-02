package com.panol_project.backendpanol.modules.catalog.location.infrastructure;

import static com.panol_project.backendpanol.jooq.tables.Location.LOCATION;

import com.panol_project.backendpanol.modules.catalog.location.domain.LocationOption;
import com.panol_project.backendpanol.modules.catalog.location.domain.LocationRepository;
import java.util.List;
import java.util.Optional;
import org.jooq.Field;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
public class LocationJooqRepository implements LocationRepository {

    private final DSLContext dsl;
    private static final Field<Boolean> LOCATION_ACTIVE = DSL.field(DSL.name("active"), Boolean.class);

    public LocationJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<LocationOption> findAll() {
        return dsl.select(LOCATION.ID, LOCATION.NAME, LOCATION.DESCRIPTION, LOCATION_ACTIVE)
                .from(LOCATION)
                .where(LOCATION_ACTIVE.isNull().or(LOCATION_ACTIVE.isTrue()))
                .orderBy(LOCATION.NAME.asc())
                .fetch(record -> new LocationOption(
                        record.get(LOCATION.ID),
                        record.get(LOCATION.NAME),
                        record.get(LOCATION.DESCRIPTION),
                        record.get(LOCATION_ACTIVE) == null || record.get(LOCATION_ACTIVE)
                ));
    }

    @Override
    public List<LocationOption> findAllForManagement() {
        return dsl.select(LOCATION.ID, LOCATION.NAME, LOCATION.DESCRIPTION, LOCATION_ACTIVE)
                .from(LOCATION)
                .orderBy(LOCATION.NAME.asc())
                .fetch(record -> new LocationOption(
                        record.get(LOCATION.ID),
                        record.get(LOCATION.NAME),
                        record.get(LOCATION.DESCRIPTION),
                        record.get(LOCATION_ACTIVE) == null || record.get(LOCATION_ACTIVE)
                ));
    }

    @Override
    public Optional<LocationOption> findById(Integer id) {
        return dsl.select(LOCATION.ID, LOCATION.NAME, LOCATION.DESCRIPTION, LOCATION_ACTIVE)
                .from(LOCATION)
                .where(LOCATION.ID.eq(id))
                .fetchOptional(record -> new LocationOption(
                        record.get(LOCATION.ID),
                        record.get(LOCATION.NAME),
                        record.get(LOCATION.DESCRIPTION),
                        record.get(LOCATION_ACTIVE) == null || record.get(LOCATION_ACTIVE)
                ));
    }

    @Override
    public boolean existsById(Integer id) {
        return dsl.fetchExists(dsl.selectOne().from(LOCATION).where(LOCATION.ID.eq(id).and(LOCATION_ACTIVE.isNull().or(LOCATION_ACTIVE.isTrue()))));
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return dsl.fetchExists(dsl.selectOne()
                .from(LOCATION)
                .where(LOCATION.NAME.equalIgnoreCase(name).and(LOCATION_ACTIVE.isNull().or(LOCATION_ACTIVE.isTrue()))));
    }

    @Override
    public boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id) {
        return dsl.fetchExists(dsl.selectOne()
                .from(LOCATION)
                .where(LOCATION.NAME.equalIgnoreCase(name)
                        .and(LOCATION.ID.ne(id))
                        .and(LOCATION_ACTIVE.isNull().or(LOCATION_ACTIVE.isTrue()))));
    }

    @Override
    public LocationOption create(String name, String description) {
        Integer id = dsl.insertInto(LOCATION)
                .set(LOCATION.NAME, name)
                .set(LOCATION.DESCRIPTION, description)
                .returning(LOCATION.ID)
                .fetchOptional(record -> record.get(LOCATION.ID))
                .orElseThrow();
        return findById(id).orElseThrow();
    }

    @Override
    public LocationOption update(Integer id, String name, String description) {
        dsl.update(LOCATION)
                .set(LOCATION.NAME, name)
                .set(LOCATION.DESCRIPTION, description)
                .where(LOCATION.ID.eq(id))
                .execute();
        return findById(id).orElseThrow();
    }

    @Override
    public int updateActive(Integer id, boolean active) {
        return dsl.update(LOCATION)
                .set(LOCATION_ACTIVE, active)
                .where(LOCATION.ID.eq(id))
                .execute();
    }
}

