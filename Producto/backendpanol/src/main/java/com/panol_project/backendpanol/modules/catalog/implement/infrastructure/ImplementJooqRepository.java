package com.panol_project.backendpanol.modules.catalog.implement.infrastructure;

import static com.panol_project.backendpanol.jooq.tables.Category.CATEGORY;
import static com.panol_project.backendpanol.jooq.tables.Implement.IMPLEMENT;
import static com.panol_project.backendpanol.jooq.tables.Location.LOCATION;
import static com.panol_project.backendpanol.jooq.tables.Stock.STOCK;

import com.panol_project.backendpanol.jooq.enums.ItemTypeEnum;
import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementCategorySummary;
import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementItemType;
import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementLocationSummary;
import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementRepository;
import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementStockSummary;
import com.panol_project.backendpanol.modules.catalog.implement.domain.ImplementSummary;
import com.panol_project.backendpanol.modules.catalog.implement.domain.Implemento;
import com.panol_project.backendpanol.modules.catalog.implement.domain.StockStatusFilter;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
public class ImplementJooqRepository implements ImplementRepository {

    private final DSLContext dsl;

    private static final Field<String> IMPLEMENT_NAME = DSL.field(DSL.name("implement", "name"), String.class);
    private static final Field<String> IMPLEMENT_DESCRIPTION = DSL.field(DSL.name("implement", "description"), String.class);
    private static final Field<String> IMPLEMENT_OBSERVATIONS = DSL.field(DSL.name("implement", "observations"), String.class);
    private static final Field<String> IMPLEMENT_BARCODE = DSL.field(DSL.name("implement", "barcode"), String.class);
    private static final Field<String> IMPLEMENT_IMG_URL = DSL.field(DSL.name("implement", "img_url"), String.class);
    private static final Field<Boolean> IMPLEMENT_ACTIVE = DSL.field(DSL.name("implement", "active"), Boolean.class);
    private static final Field<OffsetDateTime> IMPLEMENT_CREATED_AT = DSL.field(
            DSL.name("implement", "created_at"),
            OffsetDateTime.class
    );
    private static final Field<OffsetDateTime> IMPLEMENT_UPDATED_AT = DSL.field(
            DSL.name("implement", "updated_at"),
            OffsetDateTime.class
    );

    private static final Field<UUID> IMPLEMENT_UUID = DSL.field(DSL.name("implement", "uuid"), UUID.class);
    private static final Field<UUID> IMPLEMENT_CATEGORY_UUID = DSL.field(DSL.name("implement", "category_uuid"), UUID.class);
    private static final Field<UUID> IMPLEMENT_LOCATION_UUID = DSL.field(DSL.name("implement", "location_uuid"), UUID.class);

    private static final Field<UUID> CATEGORY_UUID = DSL.field(DSL.name("category", "uuid"), UUID.class);
    private static final Field<UUID> LOCATION_UUID = DSL.field(DSL.name("location", "uuid"), UUID.class);

    private static final Field<UUID> STOCK_IMPLEMENT_UUID = DSL.field(DSL.name("stock", "implement_uuid"), UUID.class);

    public ImplementJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Optional<Implemento> findByUuid(UUID uuid) {
        return dsl.select(
                        IMPLEMENT_UUID,
                        IMPLEMENT_NAME,
                        IMPLEMENT_DESCRIPTION,
                        IMPLEMENT_CATEGORY_UUID,
                        IMPLEMENT_LOCATION_UUID,
                        IMPLEMENT.ITEM_TYPE,
                        IMPLEMENT_BARCODE,
                        IMPLEMENT_IMG_URL,
                        IMPLEMENT_OBSERVATIONS,
                        IMPLEMENT_ACTIVE,
                        IMPLEMENT_CREATED_AT,
                        IMPLEMENT_UPDATED_AT
                )
                .from(IMPLEMENT)
                .where(IMPLEMENT_UUID.eq(uuid))
                .fetchOptional()
                .map(this::toDomain);
    }

    @Override
    public Optional<ImplementSummary> findSummaryByUuid(UUID uuid) {
        return dsl.select(
                        IMPLEMENT_UUID,
                        IMPLEMENT_NAME,
                        IMPLEMENT_DESCRIPTION,
                        IMPLEMENT_BARCODE,
                        IMPLEMENT_IMG_URL,
                        IMPLEMENT_ACTIVE,
                        CATEGORY_UUID,
                        CATEGORY.NAME,
                        CATEGORY.ACTIVE,
                        LOCATION_UUID,
                        LOCATION.NAME,
                        LOCATION.DESCRIPTION,
                        STOCK.TOTAL_STOCK,
                        STOCK.MIN_STOCK,
                        STOCK.AVAILABLE,
                        STOCK.RESERVED,
                        STOCK.LOANED,
                        STOCK.DAMAGED
                )
                .from(IMPLEMENT)
                .leftJoin(CATEGORY).on(CATEGORY_UUID.eq(IMPLEMENT_CATEGORY_UUID))
                .join(LOCATION).on(LOCATION_UUID.eq(IMPLEMENT_LOCATION_UUID))
                .leftJoin(STOCK).on(STOCK_IMPLEMENT_UUID.eq(IMPLEMENT_UUID))
                .where(IMPLEMENT_UUID.eq(uuid))
                .fetchOptional(record -> {
                    UUID categoryUuid = record.get(CATEGORY_UUID);
                    ImplementCategorySummary category = categoryUuid == null
                            ? null
                            : new ImplementCategorySummary(
                                    categoryUuid,
                                    record.get(CATEGORY.NAME),
                                    record.get(CATEGORY.ACTIVE)
                            );

                    return new ImplementSummary(
                            record.get(IMPLEMENT_UUID),
                            record.get(IMPLEMENT_NAME),
                            record.get(IMPLEMENT_DESCRIPTION),
                            record.get(IMPLEMENT_BARCODE),
                            record.get(IMPLEMENT_IMG_URL),
                            record.get(IMPLEMENT_ACTIVE),
                            category,
                            new ImplementLocationSummary(
                                    record.get(LOCATION_UUID),
                                    record.get(LOCATION.NAME),
                                    record.get(LOCATION.DESCRIPTION)
                            ),
                            new ImplementStockSummary(
                                    record.get(STOCK.TOTAL_STOCK),
                                    record.get(STOCK.MIN_STOCK),
                                    record.get(STOCK.AVAILABLE),
                                    record.get(STOCK.RESERVED),
                                    record.get(STOCK.LOANED),
                                    record.get(STOCK.DAMAGED)
                            )
                    );
                });
    }

    @Override
    public List<ImplementSummary> findAllSummaries(String name, UUID categoryUuid, StockStatusFilter stockStatusFilter) {
        Condition condition = IMPLEMENT_ACTIVE.isTrue();

        if (name != null) {
            condition = condition.and(DSL.lower(IMPLEMENT_NAME).like("%" + name.toLowerCase(Locale.ROOT) + "%"));
        }

        if (categoryUuid != null) {
            condition = condition.and(IMPLEMENT_CATEGORY_UUID.eq(categoryUuid));
        }

        if (stockStatusFilter != null) {
            Field<Integer> stockField = resolveStockField(stockStatusFilter);
            condition = condition.and(stockField.gt(0));
        }

        return dsl.select(
                        IMPLEMENT_UUID,
                        IMPLEMENT_NAME,
                        IMPLEMENT_DESCRIPTION,
                        IMPLEMENT_BARCODE,
                        IMPLEMENT_IMG_URL,
                        IMPLEMENT_ACTIVE,
                        CATEGORY_UUID,
                        CATEGORY.NAME,
                        CATEGORY.ACTIVE,
                        LOCATION_UUID,
                        LOCATION.NAME,
                        LOCATION.DESCRIPTION,
                        STOCK.TOTAL_STOCK,
                        STOCK.MIN_STOCK,
                        STOCK.AVAILABLE,
                        STOCK.RESERVED,
                        STOCK.LOANED,
                        STOCK.DAMAGED
                )
                .from(IMPLEMENT)
                .leftJoin(CATEGORY).on(CATEGORY_UUID.eq(IMPLEMENT_CATEGORY_UUID))
                .join(LOCATION).on(LOCATION_UUID.eq(IMPLEMENT_LOCATION_UUID))
                .leftJoin(STOCK).on(STOCK_IMPLEMENT_UUID.eq(IMPLEMENT_UUID))
                .where(condition)
                .orderBy(IMPLEMENT_NAME.asc())
                .fetch(record -> {
                    UUID summaryCategoryUuid = record.get(CATEGORY_UUID);
                    ImplementCategorySummary category = summaryCategoryUuid == null
                            ? null
                            : new ImplementCategorySummary(
                                    summaryCategoryUuid,
                                    record.get(CATEGORY.NAME),
                                    record.get(CATEGORY.ACTIVE)
                            );

                    return new ImplementSummary(
                            record.get(IMPLEMENT_UUID),
                            record.get(IMPLEMENT_NAME),
                            record.get(IMPLEMENT_DESCRIPTION),
                            record.get(IMPLEMENT_BARCODE),
                            record.get(IMPLEMENT_IMG_URL),
                            record.get(IMPLEMENT_ACTIVE),
                            category,
                            new ImplementLocationSummary(
                                    record.get(LOCATION_UUID),
                                    record.get(LOCATION.NAME),
                                    record.get(LOCATION.DESCRIPTION)
                            ),
                            new ImplementStockSummary(
                                    record.get(STOCK.TOTAL_STOCK),
                                    record.get(STOCK.MIN_STOCK),
                                    record.get(STOCK.AVAILABLE),
                                    record.get(STOCK.RESERVED),
                                    record.get(STOCK.LOANED),
                                    record.get(STOCK.DAMAGED)
                            )
                    );
                });
    }

    @Override
    public boolean existsActiveByNameIgnoreCase(String nombre) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(IMPLEMENT)
                        .where(IMPLEMENT_ACTIVE.isTrue()
                                .and(IMPLEMENT_NAME.likeIgnoreCase(nombre)))
        );
    }

    @Override
    public boolean existsActiveByNameIgnoreCaseAndUuidNot(String nombre, UUID excludedUuid) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(IMPLEMENT)
                        .where(IMPLEMENT_ACTIVE.isTrue()
                                .and(IMPLEMENT_UUID.ne(excludedUuid))
                                .and(IMPLEMENT_NAME.likeIgnoreCase(nombre)))
        );
    }

    @Override
    public Implemento create(
            String nombre,
            String descripcion,
            UUID categoriaUuid,
            UUID locationUuid,
            ImplementItemType itemType,
            String barcode,
            String imgUrl,
            String observations
    ) {
        return dsl.insertInto(IMPLEMENT)
                .set(IMPLEMENT_NAME, nombre)
                .set(IMPLEMENT_DESCRIPTION, descripcion)
                .set(IMPLEMENT_CATEGORY_UUID, categoriaUuid)
                .set(IMPLEMENT_LOCATION_UUID, locationUuid)
                .set(IMPLEMENT.ITEM_TYPE, toJooqItemType(itemType))
                .set(IMPLEMENT_BARCODE, barcode)
                .set(IMPLEMENT_IMG_URL, imgUrl)
                .set(IMPLEMENT_OBSERVATIONS, observations)
                .returningResult(
                        IMPLEMENT_UUID,
                        IMPLEMENT_NAME,
                        IMPLEMENT_DESCRIPTION,
                        IMPLEMENT_CATEGORY_UUID,
                        IMPLEMENT_LOCATION_UUID,
                        IMPLEMENT.ITEM_TYPE,
                        IMPLEMENT_BARCODE,
                        IMPLEMENT_IMG_URL,
                        IMPLEMENT_OBSERVATIONS,
                        IMPLEMENT_ACTIVE,
                        IMPLEMENT_CREATED_AT,
                        IMPLEMENT_UPDATED_AT
                )
                .fetchOptional()
                .map(this::toDomain)
                .orElseThrow();
    }

    @Override
    public Implemento update(
            UUID uuid,
            String nombre,
            String descripcion,
            UUID categoriaUuid,
            UUID locationUuid,
            ImplementItemType itemType,
            String barcode,
            String imgUrl,
            String observations
    ) {
        return dsl.update(IMPLEMENT)
                .set(IMPLEMENT_NAME, nombre)
                .set(IMPLEMENT_DESCRIPTION, descripcion)
                .set(IMPLEMENT_CATEGORY_UUID, categoriaUuid)
                .set(IMPLEMENT_LOCATION_UUID, locationUuid)
                .set(IMPLEMENT.ITEM_TYPE, toJooqItemType(itemType))
                .set(IMPLEMENT_BARCODE, barcode)
                .set(IMPLEMENT_IMG_URL, imgUrl)
                .set(IMPLEMENT_OBSERVATIONS, observations)
                .set(IMPLEMENT_UPDATED_AT, OffsetDateTime.now())
                .where(IMPLEMENT_UUID.eq(uuid))
                .returningResult(
                        IMPLEMENT_UUID,
                        IMPLEMENT_NAME,
                        IMPLEMENT_DESCRIPTION,
                        IMPLEMENT_CATEGORY_UUID,
                        IMPLEMENT_LOCATION_UUID,
                        IMPLEMENT.ITEM_TYPE,
                        IMPLEMENT_BARCODE,
                        IMPLEMENT_IMG_URL,
                        IMPLEMENT_OBSERVATIONS,
                        IMPLEMENT_ACTIVE,
                        IMPLEMENT_CREATED_AT,
                        IMPLEMENT_UPDATED_AT
                )
                .fetchOptional()
                .map(this::toDomain)
                .orElseThrow();
    }

    @Override
    public int updateMinStockByImplementUuid(UUID implementUuid, Integer minStock) {
        return dsl.insertInto(STOCK)
                .set(STOCK_IMPLEMENT_UUID, implementUuid)
                .set(STOCK.MIN_STOCK, minStock)
                .onConflict(STOCK_IMPLEMENT_UUID)
                .doUpdate()
                .set(STOCK.MIN_STOCK, minStock)
                .execute();
    }

    @Override
    public Optional<Integer> findMinStockByImplementUuid(UUID implementUuid) {
        return dsl.select(STOCK.MIN_STOCK)
                .from(STOCK)
                .where(STOCK_IMPLEMENT_UUID.eq(implementUuid))
                .fetchOptional(STOCK.MIN_STOCK);
    }

    @Override
    public int updateActive(UUID uuid, boolean active) {
        return dsl.update(IMPLEMENT)
                .set(IMPLEMENT_ACTIVE, active)
                .set(IMPLEMENT_UPDATED_AT, OffsetDateTime.now())
                .where(IMPLEMENT_UUID.eq(uuid))
                .execute();
    }

    private Field<Integer> resolveStockField(StockStatusFilter filter) {
        return switch (filter) {
            case AVAILABLE -> STOCK.AVAILABLE;
            case RESERVED -> STOCK.RESERVED;
            case LOANED -> STOCK.LOANED;
            case DAMAGED -> STOCK.DAMAGED;
            case BLOCKED -> DSL.field(DSL.name("stock", "blocked"), Integer.class);
        };
    }

    private Implemento toDomain(Record record) {
        return new Implemento(
                record.get(IMPLEMENT_UUID),
                record.get(IMPLEMENT_NAME),
                record.get(IMPLEMENT_DESCRIPTION),
                record.get(IMPLEMENT_CATEGORY_UUID),
                record.get(IMPLEMENT_LOCATION_UUID),
                toDomainItemType(record.get(IMPLEMENT.ITEM_TYPE)),
                record.get(IMPLEMENT_BARCODE),
                record.get(IMPLEMENT_IMG_URL),
                record.get(IMPLEMENT_OBSERVATIONS),
                record.get(IMPLEMENT_ACTIVE),
                record.get(IMPLEMENT_CREATED_AT),
                record.get(IMPLEMENT_UPDATED_AT)
        );
    }

    private ImplementItemType toDomainItemType(ItemTypeEnum itemType) {
        return itemType == null
                ? null
                : ImplementItemType.fromLiteral(itemType.getLiteral()).orElse(null);
    }

    private ItemTypeEnum toJooqItemType(ImplementItemType itemType) {
        return ItemTypeEnum.lookupLiteral(Objects.requireNonNull(itemType, "itemType").literal());
    }
}
