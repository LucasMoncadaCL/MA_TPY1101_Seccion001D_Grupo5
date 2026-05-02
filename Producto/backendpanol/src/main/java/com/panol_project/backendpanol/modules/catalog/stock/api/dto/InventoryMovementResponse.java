package com.panol_project.backendpanol.modules.catalog.stock.api.dto;

import com.panol_project.backendpanol.modules.catalog.stock.domain.MovementAction;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record InventoryMovementResponse(
        String id,
        @JsonProperty("implement_id")
        Integer implementId,
        MovementAction action,
        Integer quantity,
        @JsonProperty("performed_by")
        String performedBy,
        Instant timestamp,
        String notes
) {
}
