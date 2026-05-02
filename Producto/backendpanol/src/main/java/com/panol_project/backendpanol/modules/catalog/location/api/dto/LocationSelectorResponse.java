package com.panol_project.backendpanol.modules.catalog.location.api.dto;

public record LocationSelectorResponse(
        Integer id,
        String name,
        String description,
        Boolean active
) {
}

