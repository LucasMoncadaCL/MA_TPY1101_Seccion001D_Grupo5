package com.panol_project.backendpanol.modules.catalog.implement.api.dto;

import java.time.OffsetDateTime;

public record ImplementResponse(
        Integer id,
        String name,
        String description,
        Integer categoryId,
        Integer locationId,
        Boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
