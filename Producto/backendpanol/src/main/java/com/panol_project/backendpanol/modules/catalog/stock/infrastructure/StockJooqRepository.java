package com.panol_project.backendpanol.modules.catalog.stock.infrastructure;

import static com.panol_project.backendpanol.jooq.tables.Implement.IMPLEMENT;
import static com.panol_project.backendpanol.jooq.tables.Individual.INDIVIDUAL;
import static com.panol_project.backendpanol.jooq.tables.Stock.STOCK;

import com.panol_project.backendpanol.jooq.enums.IndividualConditionEnum;
import com.panol_project.backendpanol.jooq.enums.IndividualStatusEnum;
import com.panol_project.backendpanol.jooq.enums.ItemTypeEnum;
import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementItemType;
import com.panol_project.backendpanol.modules.catalog.stock.domain.IndividualItem;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockCounters;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class StockJooqRepository implements StockRepository {

    private final DSLContext dsl;

    public StockJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Optional<ImplementStockContext> findImplementContext(Integer implementId) {
        return dsl.select(IMPLEMENT.ID, IMPLEMENT.LOCATION_ID, IMPLEMENT.ITEM_TYPE, IMPLEMENT.ACTIVE)
                .from(IMPLEMENT)
                .where(IMPLEMENT.ID.eq(implementId))
                .fetchOptional(record -> new ImplementStockContext(
                        record.get(IMPLEMENT.ID),
                        record.get(IMPLEMENT.LOCATION_ID),
                        toDomainItemType(record.get(IMPLEMENT.ITEM_TYPE)),
                        record.get(IMPLEMENT.ACTIVE)
                ));
    }

    @Override
    public void ensureStockRow(Integer implementId) {
        dsl.insertInto(STOCK)
                .set(STOCK.IMPLEMENT_ID, implementId)
                .onConflict(STOCK.IMPLEMENT_ID)
                .doNothing()
                .execute();
    }

    @Override
    public Optional<StockCounters> findStockByImplementId(Integer implementId) {
        return dsl.select(STOCK.TOTAL_STOCK, STOCK.MIN_STOCK, STOCK.AVAILABLE, STOCK.RESERVED, STOCK.LOANED, STOCK.DAMAGED)
                .from(STOCK)
                .where(STOCK.IMPLEMENT_ID.eq(implementId))
                .fetchOptional(record -> new StockCounters(
                        record.get(STOCK.TOTAL_STOCK),
                        record.get(STOCK.MIN_STOCK),
                        record.get(STOCK.AVAILABLE),
                        record.get(STOCK.RESERVED),
                        record.get(STOCK.LOANED),
                        record.get(STOCK.DAMAGED)
                ));
    }

    @Override
    public List<IndividualItem> findActiveIndividualsByImplementId(Integer implementId) {
        return dsl.selectFrom(INDIVIDUAL)
                .where(INDIVIDUAL.IMPLEMENT_ID.eq(implementId).and(INDIVIDUAL.ACTIVE.isTrue()))
                .orderBy(INDIVIDUAL.ID.asc())
                .fetch(record -> new IndividualItem(
                        record.getId(),
                        record.getImplementId(),
                        record.getAssetCode(),
                        record.getStatus() == null ? null : record.getStatus().getLiteral(),
                        record.getCondition() == null ? null : record.getCondition().getLiteral(),
                        record.getCurrentLocationId(),
                        record.getActive()
                ));
    }

    @Override
    public List<IndividualItem> findActiveIndividualsByIds(Integer implementId, List<Integer> individualIds) {
        if (individualIds == null || individualIds.isEmpty()) {
            return List.of();
        }

        return dsl.selectFrom(INDIVIDUAL)
                .where(INDIVIDUAL.IMPLEMENT_ID.eq(implementId)
                        .and(INDIVIDUAL.ACTIVE.isTrue())
                        .and(INDIVIDUAL.ID.in(individualIds)))
                .orderBy(INDIVIDUAL.ID.asc())
                .fetch(record -> new IndividualItem(
                        record.getId(),
                        record.getImplementId(),
                        record.getAssetCode(),
                        record.getStatus() == null ? null : record.getStatus().getLiteral(),
                        record.getCondition() == null ? null : record.getCondition().getLiteral(),
                        record.getCurrentLocationId(),
                        record.getActive()
                ));
    }

    @Override
    public void createIndividuals(Integer implementId, Integer locationId, List<String> assetCodes) {
        if (assetCodes == null || assetCodes.isEmpty()) {
            return;
        }

        var now = OffsetDateTime.now();
        var insert = dsl.insertInto(
                INDIVIDUAL,
                INDIVIDUAL.IMPLEMENT_ID,
                INDIVIDUAL.ASSET_CODE,
                INDIVIDUAL.STATUS,
                INDIVIDUAL.CONDITION,
                INDIVIDUAL.CURRENT_LOCATION_ID,
                INDIVIDUAL.ACTIVE,
                INDIVIDUAL.CREATED_AT,
                INDIVIDUAL.UPDATED_AT
        );

        for (String code : assetCodes) {
            insert = insert.values(
                    implementId,
                    code,
                    IndividualStatusEnum.available,
                    IndividualConditionEnum.good,
                    locationId,
                    true,
                    now,
                    now
            );
        }

        insert.execute();
    }

    @Override
    public void updateStock(Integer implementId, int totalDelta, int availableDelta, int reservedDelta, int loanedDelta, int damagedDelta) {
        dsl.update(STOCK)
                .set(STOCK.TOTAL_STOCK, STOCK.TOTAL_STOCK.add(totalDelta))
                .set(STOCK.AVAILABLE, STOCK.AVAILABLE.add(availableDelta))
                .set(STOCK.RESERVED, STOCK.RESERVED.add(reservedDelta))
                .set(STOCK.LOANED, STOCK.LOANED.add(loanedDelta))
                .set(STOCK.DAMAGED, STOCK.DAMAGED.add(damagedDelta))
                .set(STOCK.UPDATED_AT, OffsetDateTime.now())
                .where(STOCK.IMPLEMENT_ID.eq(implementId))
                .execute();
    }

    @Override
    public void updateIndividualsState(List<Integer> individualIds, String statusLiteral, String conditionLiteral, Integer locationId, Boolean active) {
        if (individualIds == null || individualIds.isEmpty()) {
            return;
        }

        var update = dsl.update(INDIVIDUAL)
                .set(INDIVIDUAL.UPDATED_AT, OffsetDateTime.now());

        if (statusLiteral != null) {
            update = update.set(INDIVIDUAL.STATUS, IndividualStatusEnum.lookupLiteral(statusLiteral));
        }
        if (conditionLiteral != null) {
            update = update.set(INDIVIDUAL.CONDITION, IndividualConditionEnum.lookupLiteral(conditionLiteral));
        }
        if (locationId != null) {
            update = update.set(INDIVIDUAL.CURRENT_LOCATION_ID, locationId);
        }
        if (active != null) {
            update = update.set(INDIVIDUAL.ACTIVE, active);
        }

        update.where(INDIVIDUAL.ID.in(individualIds)).execute();
    }

    private ImplementItemType toDomainItemType(ItemTypeEnum itemType) {
        return itemType == null
                ? null
                : ImplementItemType.fromLiteral(itemType.getLiteral()).orElse(null);
    }
}
