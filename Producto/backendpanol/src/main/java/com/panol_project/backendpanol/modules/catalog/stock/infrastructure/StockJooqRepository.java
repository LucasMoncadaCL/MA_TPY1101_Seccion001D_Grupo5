package com.panol_project.backendpanol.modules.catalog.stock.infrastructure;

import static com.panol_project.backendpanol.jooq.tables.Implement.IMPLEMENT;
import static com.panol_project.backendpanol.jooq.tables.Individual.INDIVIDUAL;
import static com.panol_project.backendpanol.jooq.tables.Stock.STOCK;

import com.panol_project.backendpanol.jooq.enums.IndividualConditionEnum;
import com.panol_project.backendpanol.jooq.enums.IndividualStatusEnum;
import com.panol_project.backendpanol.jooq.enums.ItemTypeEnum;
import com.panol_project.backendpanol.modules.catalog.stock.domain.IndividualItem;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockCounters;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockItemType;
import com.panol_project.backendpanol.modules.catalog.stock.domain.StockRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
public class StockJooqRepository implements StockRepository {

    private final DSLContext dsl;

    private static final Field<UUID> IMPLEMENT_UUID = DSL.field(DSL.name("implement", "uuid"), UUID.class);
    private static final Field<UUID> IMPLEMENT_LOCATION_UUID = DSL.field(DSL.name("implement", "location_uuid"), UUID.class);

    private static final Field<UUID> STOCK_IMPLEMENT_UUID = DSL.field(DSL.name("stock", "implement_uuid"), UUID.class);

    private static final Field<UUID> INDIVIDUAL_UUID = DSL.field(DSL.name("individual", "uuid"), UUID.class);
    private static final Field<UUID> INDIVIDUAL_IMPLEMENT_UUID = DSL.field(DSL.name("individual", "implement_uuid"), UUID.class);
    private static final Field<UUID> INDIVIDUAL_CURRENT_LOCATION_UUID = DSL.field(DSL.name("individual", "current_location_uuid"), UUID.class);

    public StockJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Optional<ImplementStockContext> findImplementContext(UUID implementUuid) {
        return dsl.select(IMPLEMENT_UUID, IMPLEMENT_LOCATION_UUID, IMPLEMENT.ITEM_TYPE, IMPLEMENT.ACTIVE)
                .from(IMPLEMENT)
                .where(IMPLEMENT_UUID.eq(implementUuid))
                .fetchOptional(record -> new ImplementStockContext(
                        record.get(IMPLEMENT_UUID),
                        record.get(IMPLEMENT_LOCATION_UUID),
                        toStockItemType(record.get(IMPLEMENT.ITEM_TYPE)),
                        record.get(IMPLEMENT.ACTIVE)
                ));
    }

    @Override
    public void ensureStockRow(UUID implementUuid) {
        dsl.insertInto(STOCK)
                .set(STOCK_IMPLEMENT_UUID, implementUuid)
                .onConflict(STOCK_IMPLEMENT_UUID)
                .doNothing()
                .execute();
    }

    @Override
    public Optional<StockCounters> findStockByImplementUuid(UUID implementUuid) {
        return dsl.select(STOCK.TOTAL_STOCK, STOCK.MIN_STOCK, STOCK.AVAILABLE, STOCK.RESERVED, STOCK.LOANED, STOCK.DAMAGED)
                .from(STOCK)
                .where(STOCK_IMPLEMENT_UUID.eq(implementUuid))
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
    public List<IndividualItem> findActiveIndividualsByImplementUuid(UUID implementUuid) {
        return dsl.select(
                        INDIVIDUAL_UUID,
                        INDIVIDUAL_IMPLEMENT_UUID,
                        INDIVIDUAL.ASSET_CODE,
                        INDIVIDUAL.STATUS,
                        INDIVIDUAL.CONDITION,
                        INDIVIDUAL_CURRENT_LOCATION_UUID,
                        INDIVIDUAL.ACTIVE
                )
                .from(INDIVIDUAL)
                .where(INDIVIDUAL_IMPLEMENT_UUID.eq(implementUuid).and(INDIVIDUAL.ACTIVE.isTrue()))
                .orderBy(INDIVIDUAL_UUID.asc())
                .fetch(record -> new IndividualItem(
                        record.get(INDIVIDUAL_UUID),
                        record.get(INDIVIDUAL_IMPLEMENT_UUID),
                        record.get(INDIVIDUAL.ASSET_CODE),
                        record.get(INDIVIDUAL.STATUS) == null ? null : record.get(INDIVIDUAL.STATUS).getLiteral(),
                        record.get(INDIVIDUAL.CONDITION) == null ? null : record.get(INDIVIDUAL.CONDITION).getLiteral(),
                        record.get(INDIVIDUAL_CURRENT_LOCATION_UUID),
                        record.get(INDIVIDUAL.ACTIVE)
                ));
    }

    @Override
    public List<IndividualItem> findActiveIndividualsByUuids(UUID implementUuid, List<UUID> individualUuids) {
        if (individualUuids == null || individualUuids.isEmpty()) {
            return List.of();
        }

        return dsl.select(
                        INDIVIDUAL_UUID,
                        INDIVIDUAL_IMPLEMENT_UUID,
                        INDIVIDUAL.ASSET_CODE,
                        INDIVIDUAL.STATUS,
                        INDIVIDUAL.CONDITION,
                        INDIVIDUAL_CURRENT_LOCATION_UUID,
                        INDIVIDUAL.ACTIVE
                )
                .from(INDIVIDUAL)
                .where(INDIVIDUAL_IMPLEMENT_UUID.eq(implementUuid)
                        .and(INDIVIDUAL.ACTIVE.isTrue())
                        .and(INDIVIDUAL_UUID.in(individualUuids)))
                .orderBy(INDIVIDUAL_UUID.asc())
                .fetch(record -> new IndividualItem(
                        record.get(INDIVIDUAL_UUID),
                        record.get(INDIVIDUAL_IMPLEMENT_UUID),
                        record.get(INDIVIDUAL.ASSET_CODE),
                        record.get(INDIVIDUAL.STATUS) == null ? null : record.get(INDIVIDUAL.STATUS).getLiteral(),
                        record.get(INDIVIDUAL.CONDITION) == null ? null : record.get(INDIVIDUAL.CONDITION).getLiteral(),
                        record.get(INDIVIDUAL_CURRENT_LOCATION_UUID),
                        record.get(INDIVIDUAL.ACTIVE)
                ));
    }

    @Override
    public void createIndividuals(UUID implementUuid, UUID locationUuid, List<String> assetCodes) {
        if (assetCodes == null || assetCodes.isEmpty()) {
            return;
        }

        var now = OffsetDateTime.now();
        var insert = dsl.insertInto(
                INDIVIDUAL,
                INDIVIDUAL_IMPLEMENT_UUID,
                INDIVIDUAL.ASSET_CODE,
                INDIVIDUAL.STATUS,
                INDIVIDUAL.CONDITION,
                INDIVIDUAL_CURRENT_LOCATION_UUID,
                INDIVIDUAL.ACTIVE,
                INDIVIDUAL.CREATED_AT,
                INDIVIDUAL.UPDATED_AT
        );

        for (String code : assetCodes) {
            insert = insert.values(
                    implementUuid,
                    code,
                    IndividualStatusEnum.available,
                    IndividualConditionEnum.good,
                    locationUuid,
                    true,
                    now,
                    now
            );
        }

        insert.execute();
    }

    @Override
    public void updateStock(UUID implementUuid, int totalDelta, int availableDelta, int reservedDelta, int loanedDelta, int damagedDelta) {
        dsl.update(STOCK)
                .set(STOCK.TOTAL_STOCK, STOCK.TOTAL_STOCK.add(totalDelta))
                .set(STOCK.AVAILABLE, STOCK.AVAILABLE.add(availableDelta))
                .set(STOCK.RESERVED, STOCK.RESERVED.add(reservedDelta))
                .set(STOCK.LOANED, STOCK.LOANED.add(loanedDelta))
                .set(STOCK.DAMAGED, STOCK.DAMAGED.add(damagedDelta))
                .set(STOCK.UPDATED_AT, OffsetDateTime.now())
                .where(STOCK_IMPLEMENT_UUID.eq(implementUuid))
                .execute();
    }

    @Override
    public void replaceStock(UUID implementUuid, int total, int available, int reserved, int loaned, int damaged) {
        dsl.update(STOCK)
                .set(STOCK.TOTAL_STOCK, total)
                .set(STOCK.AVAILABLE, available)
                .set(STOCK.RESERVED, reserved)
                .set(STOCK.LOANED, loaned)
                .set(STOCK.DAMAGED, damaged)
                .set(STOCK.UPDATED_AT, OffsetDateTime.now())
                .where(STOCK_IMPLEMENT_UUID.eq(implementUuid))
                .execute();
    }

    @Override
    public void updateIndividualsState(List<UUID> individualUuids, String statusLiteral, String conditionLiteral, UUID locationUuid, Boolean active) {
        if (individualUuids == null || individualUuids.isEmpty()) {
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
        if (locationUuid != null) {
            update = update.set(INDIVIDUAL_CURRENT_LOCATION_UUID, locationUuid);
        }
        if (active != null) {
            update = update.set(INDIVIDUAL.ACTIVE, active);
        }

        update.where(INDIVIDUAL_UUID.in(individualUuids)).execute();
    }

    private StockItemType toStockItemType(ItemTypeEnum itemType) {
        return itemType == null
                ? null
                : StockItemType.fromLiteral(itemType.getLiteral()).orElse(null);
    }
}
