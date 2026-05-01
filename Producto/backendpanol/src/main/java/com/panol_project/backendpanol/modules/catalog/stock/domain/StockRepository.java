package com.panol_project.backendpanol.modules.catalog.stock.domain;

import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementItemType;
import java.util.List;
import java.util.Optional;

public interface StockRepository {

    Optional<ImplementStockContext> findImplementContext(Integer implementId);

    void ensureStockRow(Integer implementId);

    Optional<StockCounters> findStockByImplementId(Integer implementId);

    List<IndividualItem> findActiveIndividualsByImplementId(Integer implementId);

    List<IndividualItem> findActiveIndividualsByIds(Integer implementId, List<Integer> individualIds);

    void createIndividuals(Integer implementId, Integer locationId, List<String> assetCodes);

    void updateStock(Integer implementId, int totalDelta, int availableDelta, int reservedDelta, int loanedDelta, int damagedDelta);

    void updateIndividualsState(List<Integer> individualIds, String statusLiteral, String conditionLiteral, Integer locationId, Boolean active);

    record ImplementStockContext(Integer implementId, Integer locationId, ImplementItemType itemType, Boolean active) {
    }
}
