package com.panol_project.backendpanol.shared.outbox.infrastructure;

import com.panol_project.backendpanol.shared.outbox.domain.OutboxEvent;
import com.panol_project.backendpanol.shared.outbox.domain.OutboxPublisher;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Map;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class MongoOutboxPublisher implements OutboxPublisher {

    private final MongoTemplate mongoTemplate;

    public MongoOutboxPublisher(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void publish(OutboxEvent event) {
        Query query = Query.query(Criteria.where("event_id").is(event.eventId().toString()));
        Update update = new Update()
                .set("event_id", event.eventId().toString())
                .set("aggregate_type", event.aggregateType())
                .set("aggregate_id", event.aggregateId() == null ? null : event.aggregateId().toString())
                .set("event_type", event.eventType())
                .set("payload", event.payload())
                .set("occurred_at", toDate(event.occurredAt()))
                .set("published_at", toDate(OffsetDateTime.now()))
                .set("source", "outbox");
        mongoTemplate.upsert(query, update, "integration_events");
    }

    private Date toDate(OffsetDateTime value) {
        return value == null ? null : Date.from(value.toInstant());
    }
}
