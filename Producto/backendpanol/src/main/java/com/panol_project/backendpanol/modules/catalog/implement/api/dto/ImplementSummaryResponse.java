package com.panol_project.backendpanol.modules.catalog.implement.api.dto;

public record ImplementSummaryResponse(
        Integer id,
        String name,
        ImplementCategorySummaryResponse category,
        ImplementLocationSummaryResponse location
) {
}

