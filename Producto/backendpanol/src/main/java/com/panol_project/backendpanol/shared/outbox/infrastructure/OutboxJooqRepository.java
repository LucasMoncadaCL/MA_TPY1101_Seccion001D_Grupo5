package com.panol_project.backendpanol.shared.outbox.infrastructure;

import com.panol_project.backendpanol.shared.outbox.domain.OutboxEvent;
import com.panol_project.backendpanol.shared.outbox.domain.OutboxEventStatus;
import com.panol_project.backendpanol.shared.outbox.domain.OutboxRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

@Repository
public class OutboxJooqRepository implements OutboxRepository {

    private final DSLContext dsl;

    public OutboxJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void addPending(UUID eventId, String aggregateType, UUID aggregateId, String eventType, String payload, OffsetDateTime occurredAt) {
        dsl.insertInto(table(name("outbox_events")))
                .columns(
                        field(name("event_id")),
                        field(name("aggregate_type")),
                        field(name("aggregate_id")),
                        field(name("event_type")),
                        field(name("payload")),
                        field(name("occurred_at")),
                        field(name("status")),
                        field(name("retry_count")))
                .values(eventId, aggregateType, aggregateId, eventType, payload, occurredAt, OutboxEventStatus.PENDING.name(), 0)
                .execute();
    }

    @Override
    public List<OutboxEvent> findPending(int limit) {
        return dsl.select(
                        field(name("event_id"), UUID.class),
                        field(name("aggregate_type"), String.class),
                        field(name("aggregate_id"), UUID.class),
                        field(name("event_type"), String.class),
                        field(name("payload"), String.class),
                        field(name("occurred_at"), OffsetDateTime.class),
                        field(name("processed_at"), OffsetDateTime.class),
                        field(name("retry_count"), Integer.class),
                        field(name("status"), String.class))
                .from(table(name("outbox_events")))
                .where(field(name("status")).eq(OutboxEventStatus.PENDING.name()))
                .orderBy(field(name("occurred_at")).asc())
                .limit(limit)
                .fetch(record -> new OutboxEvent(
                        record.value1(),
                        record.value2(),
                        record.value3(),
                        record.value4(),
                        record.value5(),
                        record.value6(),
                        record.value7(),
                        record.value8(),
                        OutboxEventStatus.valueOf(record.value9())
                ));
    }

    @Override
    public void markProcessed(UUID eventId, OffsetDateTime processedAt) {
        dsl.update(table(name("outbox_events")))
                .set(field(name("status")), OutboxEventStatus.PROCESSED.name())
                .set(field(name("processed_at")), processedAt)
                .where(field(name("event_id"), UUID.class).eq(eventId))
                .execute();
    }

    @Override
    public void markRetry(UUID eventId, int retryCount, OutboxEventStatus status) {
        dsl.update(table(name("outbox_events")))
                .set(field(name("retry_count")), retryCount)
                .set(field(name("status")), status.name())
                .where(field(name("event_id"), UUID.class).eq(eventId))
                .execute();
    }

    @Override
    public int countByStatus(OutboxEventStatus status) {
        Integer count = dsl.selectCount()
                .from(table(name("outbox_events")))
                .where(field(name("status")).eq(status.name()))
                .fetchOne(0, Integer.class);
        return count == null ? 0 : count;
    }

    @Override
    public int countWithRetries() {
        Integer count = dsl.selectCount()
                .from(table(name("outbox_events")))
                .where(field(name("retry_count"), Integer.class).gt(0))
                .fetchOne(0, Integer.class);
        return count == null ? 0 : count;
    }
}
