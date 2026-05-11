package com.panol_project.backendpanol.modules.catalog.implement.domain;

import java.util.UUID;

public record ImplementSummary(
        UUID uuid,
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

