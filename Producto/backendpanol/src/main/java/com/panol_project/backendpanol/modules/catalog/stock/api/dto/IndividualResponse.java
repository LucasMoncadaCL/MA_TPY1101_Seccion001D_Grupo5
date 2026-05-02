package com.panol_project.backendpanol.modules.catalog.stock.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IndividualResponse(
        Integer id,
        @JsonProperty("asset_code")
        String assetCode,
        String status,
        String condition,
        @JsonProperty("current_location_id")
        Integer currentLocationId,
        Boolean active
) {
}
