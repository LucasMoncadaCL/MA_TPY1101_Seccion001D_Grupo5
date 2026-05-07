package com.panol_project.backendpanol.modules.auth.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

@Service
public class AuditLogService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditLogService.class);

    private final DSLContext dsl;
    private final ObjectMapper objectMapper;

    public AuditLogService(DSLContext dsl, ObjectMapper objectMapper) {
        this.dsl = dsl;
        this.objectMapper = objectMapper;
    }

    public void log(String event, Integer actorUserId, Integer targetUserId, Map<String, Object> payload) {
        try {
            dsl.insertInto(table(name("audit_log")))
                    .columns(field(name("event")), field(name("actor_user_id")), field(name("target_user_id")), field(name("payload")))
                    .values(event, actorUserId, targetUserId, objectMapper.writeValueAsString(payload))
                    .execute();
        } catch (JsonProcessingException ex) {
            LOG.warn("audit_log_payload_serialize_failed", ex);
        } catch (Exception ex) {
            LOG.warn("audit_log_insert_failed", ex);
        }
    }
}

