package com.panol_project.backendpanol.modules.catalog.stock.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IndividualUpdateRequest(
        String status,
        String condition,
        @JsonProperty("current_location_id")
        Integer currentLocationId,
        Boolean active
) {
}
