package com.panol_project.backendpanol.modules.catalog.location.api;

import com.panol_project.backendpanol.modules.catalog.location.api.dto.LocationSelectorResponse;
import com.panol_project.backendpanol.modules.catalog.location.application.LocationService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService service;

    public LocationController(LocationService service) {
        this.service = service;
    }

    @GetMapping
    public List<LocationSelectorResponse> listarSelector() {
        return service.listarSelector().stream()
                .map(location -> new LocationSelectorResponse(location.id(), location.name(), location.description()))
                .toList();
    }
}

