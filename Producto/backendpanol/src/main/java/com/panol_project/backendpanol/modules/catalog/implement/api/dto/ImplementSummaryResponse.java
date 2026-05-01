package com.panol_project.backendpanol.modules.catalog.implement.api.dto;

public record ImplementSummaryResponse(
        Integer id,
        String name,
        String description,
        String barcode,
        String imgUrl,
        Boolean active,
        Boolean available,
        ImplementCategorySummaryResponse category,
        ImplementLocationSummaryResponse location,
        ImplementStockSummaryResponse stock
) {
}

