package com.panol_project.backendpanol.modules.catalog.location.infrastructure;

import static com.panol_project.backendpanol.jooq.tables.Location.LOCATION;

import com.panol_project.backendpanol.modules.catalog.location.domain.LocationOption;
import com.panol_project.backendpanol.modules.catalog.location.domain.LocationRepository;
import java.util.List;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class LocationJooqRepository implements LocationRepository {

    private final DSLContext dsl;

    public LocationJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<LocationOption> findAll() {
        return dsl.select(LOCATION.ID, LOCATION.NAME)
                .from(LOCATION)
                .orderBy(LOCATION.NAME.asc())
                .fetch(record -> new LocationOption(
                        record.get(LOCATION.ID),
                        record.get(LOCATION.NAME)
                ));
    }

    @Override
    public boolean existsById(Integer id) {
        return dsl.fetchExists(dsl.selectOne().from(LOCATION).where(LOCATION.ID.eq(id)));
    }
}

