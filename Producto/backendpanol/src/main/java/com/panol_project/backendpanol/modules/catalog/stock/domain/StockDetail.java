package com.panol_project.backendpanol.modules.catalog.stock.domain;

import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementItemType;
import java.util.List;
import java.util.UUID;

public record StockDetail(
        UUID implementUuid,
        ImplementItemType itemType,
        StockCounters stock,
        List<IndividualItem> individuals
) {
}
