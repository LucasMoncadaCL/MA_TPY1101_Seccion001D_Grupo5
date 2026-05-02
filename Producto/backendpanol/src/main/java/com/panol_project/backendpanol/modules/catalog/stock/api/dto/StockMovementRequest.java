package com.panol_project.backendpanol.modules.catalog.stock.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StockMovementRequest(
        @JsonProperty("movement_type")
        String movementType,
        Integer quantity,
        @JsonProperty("individual_ids")
        List<Integer> individualIds,
        String condition
) {
}
