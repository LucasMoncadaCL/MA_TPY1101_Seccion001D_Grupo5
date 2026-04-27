package com.panol_project.backendpanol.modules.catalog.location.application;

import com.panol_project.backendpanol.modules.catalog.location.domain.LocationOption;
import com.panol_project.backendpanol.modules.catalog.location.domain.LocationRepository;
import com.panol_project.backendpanol.shared.error.BadRequestException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationService {

    private final LocationRepository repository;

    public LocationService(LocationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<LocationOption> listarSelector() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public void validarLocationExistente(Integer locationId) {
        if (locationId == null) {
            throw new BadRequestException("LOCATION_REQUIRED", "La ubicacion es obligatoria");
        }
        if (!repository.existsById(locationId)) {
            throw new BadRequestException("LOCATION_NOT_FOUND", "La ubicacion no existe");
        }
    }
}

