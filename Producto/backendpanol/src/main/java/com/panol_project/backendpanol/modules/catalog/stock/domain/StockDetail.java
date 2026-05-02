package com.panol_project.backendpanol.modules.catalog.stock.domain;

import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementItemType;
import java.util.List;

public record StockDetail(
        Integer implementId,
        ImplementItemType itemType,
        StockCounters stock,
        List<IndividualItem> individuals
) {
}
