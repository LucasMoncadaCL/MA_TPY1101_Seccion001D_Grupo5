package com.panol_project.backendpanol.modules.catalog.category.api.dto;

import java.time.OffsetDateTime;

public record CategoriaResponse(
        Integer id,
        String nombre,
        String descripcion,
        Boolean activa,
        OffsetDateTime createdAt
) {
}
