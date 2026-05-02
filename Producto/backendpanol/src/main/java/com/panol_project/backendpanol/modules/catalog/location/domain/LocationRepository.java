package com.panol_project.backendpanol.modules.catalog.location.domain;

import java.util.List;
import java.util.Optional;

public interface LocationRepository {

    List<LocationOption> findAll();

    List<LocationOption> findAllForManagement();

    Optional<LocationOption> findById(Integer id);

    boolean existsById(Integer id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);

    LocationOption create(String name, String description);

    LocationOption update(Integer id, String name, String description);

    int updateActive(Integer id, boolean active);
}

