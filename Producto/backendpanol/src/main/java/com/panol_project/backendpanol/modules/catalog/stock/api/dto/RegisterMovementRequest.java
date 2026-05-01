package com.panol_project.backendpanol.modules.catalog.stock.api.dto;

import jakarta.validation.constraints.NotNull;

public record RegisterMovementRequest(
        @NotNull(message = "El campo action es obligatorio")
        ManualMovementType action,
        
        @NotNull(message = "El campo quantity es obligatorio")
        Integer quantity,
        
        String notes
) {
}
