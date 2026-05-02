package com.panol_project.backendpanol.modules.catalog.location.application;

import com.panol_project.backendpanol.modules.catalog.location.domain.LocationOption;
import com.panol_project.backendpanol.modules.catalog.location.domain.LocationRepository;
import com.panol_project.backendpanol.shared.error.BadRequestException;
import com.panol_project.backendpanol.shared.error.NotFoundException;
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
    public List<LocationOption> listarGestion() {
        return repository.findAllForManagement();
    }

    @Transactional
    public LocationOption crear(String name, String description) {
        String normalizedName = normalizeRequired(name, "LOCATION_NAME_REQUIRED", "El nombre es obligatorio");
        String normalizedDescription = normalizeOptional(description);
        if (repository.existsByNameIgnoreCase(normalizedName)) {
            throw new BadRequestException("LOCATION_NAME_DUPLICATE", "Ya existe una ubicacion con ese nombre");
        }
        return repository.create(normalizedName, normalizedDescription);
    }

    @Transactional
    public LocationOption editar(Integer id, String name, String description) {
        requireLocation(id);
        String normalizedName = normalizeRequired(name, "LOCATION_NAME_REQUIRED", "El nombre es obligatorio");
        String normalizedDescription = normalizeOptional(description);
        if (repository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new BadRequestException("LOCATION_NAME_DUPLICATE", "Ya existe una ubicacion con ese nombre");
        }
        return repository.update(id, normalizedName, normalizedDescription);
    }

    @Transactional
    public LocationOption setActive(Integer id, boolean active) {
        LocationOption existing = requireLocation(id);
        if (Boolean.TRUE.equals(existing.active()) == active) {
            return existing;
        }
        repository.updateActive(id, active);
        return requireLocation(id);
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

    private LocationOption requireLocation(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("LOCATION_NOT_FOUND", "Ubicacion no encontrada"));
    }

    private String normalizeRequired(String value, String code, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new BadRequestException(code, message);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}

