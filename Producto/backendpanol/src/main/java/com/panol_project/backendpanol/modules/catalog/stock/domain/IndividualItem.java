package com.panol_project.backendpanol.modules.catalog.stock.domain;

public record IndividualItem(
        Integer id,
        Integer implementId,
        String assetCode,
        String status,
        String condition,
        Integer currentLocationId,
        Boolean active
) {
}
