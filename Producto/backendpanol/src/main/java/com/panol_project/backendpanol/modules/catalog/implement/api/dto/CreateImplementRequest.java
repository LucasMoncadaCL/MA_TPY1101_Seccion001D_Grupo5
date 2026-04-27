package com.panol_project.backendpanol.modules.catalog.implement.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateImplementRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String name,
        @Size(max = 255, message = "La descripcion no puede superar 255 caracteres")
        String description,
        @JsonProperty("category_id")
        Integer categoryId,
        @NotNull(message = "La ubicacion es obligatoria")
        @JsonProperty("location_id")
        Integer locationId
) {
}
