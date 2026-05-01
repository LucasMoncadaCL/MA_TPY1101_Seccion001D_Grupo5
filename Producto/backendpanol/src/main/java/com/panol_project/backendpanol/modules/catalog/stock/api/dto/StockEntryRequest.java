package com.panol_project.backendpanol.modules.catalog.stock.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record StockEntryRequest(
        @NotNull(message = "quantity es obligatorio")
        @Positive(message = "quantity debe ser un entero positivo")
        Integer quantity,
        @JsonProperty("asset_codes")
        List<String> assetCodes
) {
}
