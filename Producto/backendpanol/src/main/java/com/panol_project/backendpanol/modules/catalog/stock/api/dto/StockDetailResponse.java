package com.panol_project.backendpanol.modules.catalog.stock.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StockDetailResponse(
        @JsonProperty("implement_id")
        Integer implementId,
        @JsonProperty("item_type")
        String itemType,
        StockCountersResponse stock,
        List<IndividualResponse> individuals
) {
}
