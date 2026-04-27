package com.panol_project.backendpanol.modules.catalog.implement.domain;

public record ImplementSummary(
        Integer id,
        String name,
        ImplementCategorySummary category,
        ImplementLocationSummary location
) {
}

