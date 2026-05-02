package com.panol_project.backendpanol.modules.catalog.implement.domain;

public record ImplementSummary(
        Integer id,
        String name,
        String description,
        String barcode,
        String imgUrl,
        Boolean active,
        ImplementCategorySummary category,
        ImplementLocationSummary location,
        ImplementStockSummary stock
) {
}

