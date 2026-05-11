package com.panol_project.backendpanol.modules.catalog.location.infrastructure;

import static com.panol_project.backendpanol.jooq.tables.Location.LOCATION;

import com.panol_project.backendpanol.modules.catalog.location.domain.LocationOption;
import com.panol_project.backendpanol.modules.catalog.location.domain.LocationRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
public class LocationJooqRepository implements LocationRepository {

    private final DSLContext dsl;
    private static final Field<UUID> LOCATION_UUID = DSL.field(DSL.name("uuid"), UUID.class);
    private static final Field<Boolean> LOCATION_ACTIVE = DSL.field(DSL.name("active"), Boolean.class);

    public LocationJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<LocationOption> findAll() {
        return dsl.select(LOCATION_UUID, LOCATION.NAME, LOCATION.DESCRIPTION, LOCATION_ACTIVE)
                .from(LOCATION)
                .where(LOCATION_ACTIVE.isNull().or(LOCATION_ACTIVE.isTrue()))
                .orderBy(LOCATION.NAME.asc())
                .fetch(record -> new LocationOption(
                        record.get(LOCATION_UUID),
                        record.get(LOCATION.NAME),
                        record.get(LOCATION.DESCRIPTION),
                        record.get(LOCATION_ACTIVE) == null || record.get(LOCATION_ACTIVE)
                ));
    }

    @Override
    public List<LocationOption> findAllForManagement() {
        return dsl.select(LOCATION_UUID, LOCATION.NAME, LOCATION.DESCRIPTION, LOCATION_ACTIVE)
                .from(LOCATION)
                .orderBy(LOCATION.NAME.asc())
                .fetch(record -> new LocationOption(
                        record.get(LOCATION_UUID),
                        record.get(LOCATION.NAME),
                        record.get(LOCATION.DESCRIPTION),
                        record.get(LOCATION_ACTIVE) == null || record.get(LOCATION_ACTIVE)
                ));
    }

    @Override
    public Optional<LocationOption> findByUuid(UUID uuid) {
        return dsl.select(LOCATION_UUID, LOCATION.NAME, LOCATION.DESCRIPTION, LOCATION_ACTIVE)
                .from(LOCATION)
                .where(LOCATION_UUID.eq(uuid))
                .fetchOptional(record -> new LocationOption(
                        record.get(LOCATION_UUID),
                        record.get(LOCATION.NAME),
                        record.get(LOCATION.DESCRIPTION),
                        record.get(LOCATION_ACTIVE) == null || record.get(LOCATION_ACTIVE)
                ));
    }

    @Override
    public boolean existsByUuid(UUID uuid) {
        return dsl.fetchExists(dsl.selectOne().from(LOCATION).where(LOCATION_UUID.eq(uuid).and(LOCATION_ACTIVE.isNull().or(LOCATION_ACTIVE.isTrue()))));
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return dsl.fetchExists(dsl.selectOne()
                .from(LOCATION)
                .where(LOCATION.NAME.equalIgnoreCase(name).and(LOCATION_ACTIVE.isNull().or(LOCATION_ACTIVE.isTrue()))));
    }

    @Override
    public boolean existsByNameIgnoreCaseAndUuidNot(String name, UUID uuid) {
        return dsl.fetchExists(dsl.selectOne()
                .from(LOCATION)
                .where(LOCATION.NAME.equalIgnoreCase(name)
                        .and(LOCATION_UUID.ne(uuid))
                        .and(LOCATION_ACTIVE.isNull().or(LOCATION_ACTIVE.isTrue()))));
    }

    @Override
    public LocationOption create(String name, String description) {
        UUID uuid = dsl.insertInto(LOCATION)
                .set(LOCATION.NAME, name)
                .set(LOCATION.DESCRIPTION, description)
                .returning(LOCATION_UUID)
                .fetchOptional(record -> record.get(LOCATION_UUID))
                .orElseThrow();
        return findByUuid(uuid).orElseThrow();
    }

    @Override
    public LocationOption update(UUID uuid, String name, String description) {
        dsl.update(LOCATION)
                .set(LOCATION.NAME, name)
                .set(LOCATION.DESCRIPTION, description)
                .where(LOCATION_UUID.eq(uuid))
                .execute();
        return findByUuid(uuid).orElseThrow();
    }

    @Override
    public int updateActive(UUID uuid, boolean active) {
        return dsl.update(LOCATION)
                .set(LOCATION_ACTIVE, active)
                .where(LOCATION_UUID.eq(uuid))
                .execute();
    }
}
