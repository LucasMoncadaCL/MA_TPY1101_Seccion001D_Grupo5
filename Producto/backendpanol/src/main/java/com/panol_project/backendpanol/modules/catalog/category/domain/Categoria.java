package com.panol_project.backendpanol.modules.catalog.category.domain;

import java.time.OffsetDateTime;

public record Categoria(
        Integer id,
        String nombre,
        String descripcion,
        Boolean activa,
        OffsetDateTime createdAt
) {
}
