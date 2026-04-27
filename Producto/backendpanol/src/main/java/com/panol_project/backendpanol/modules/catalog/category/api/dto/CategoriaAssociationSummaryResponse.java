package com.panol_project.backendpanol.modules.catalog.category.api.dto;

public record CategoriaAssociationSummaryResponse(
        Integer categoriaId,
        Integer implementosAsociados,
        Boolean canDelete
) {
}
