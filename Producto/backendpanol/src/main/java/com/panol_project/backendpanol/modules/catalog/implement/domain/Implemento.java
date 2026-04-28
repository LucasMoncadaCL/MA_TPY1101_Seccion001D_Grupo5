package com.panol_project.backendpanol.modules.catalog.implement.domain;

import java.time.OffsetDateTime;

public record Implemento(
        Integer id,
        String nombre,
        String descripcion,
        Integer categoriaId,
        Integer locationId,
        ImplementItemType itemType,
        Boolean activo,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
