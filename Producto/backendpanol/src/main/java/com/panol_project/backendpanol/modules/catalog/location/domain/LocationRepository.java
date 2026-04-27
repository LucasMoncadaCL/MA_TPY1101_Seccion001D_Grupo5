package com.panol_project.backendpanol.modules.catalog.location.domain;

import java.util.List;

public interface LocationRepository {

    List<LocationOption> findAll();

    boolean existsById(Integer id);
}

