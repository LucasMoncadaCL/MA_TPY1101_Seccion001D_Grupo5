package com.panol_project.backendpanol.shared.outbox.application;

import com.panol_project.backendpanol.shared.outbox.domain.OutboxEventStatus;
import com.panol_project.backendpanol.shared.outbox.domain.OutboxPublisher;
import com.panol_project.backendpanol.shared.outbox.domain.OutboxRepository;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxWorker {

    private static final Logger LOG = LoggerFactory.getLogger(OutboxWorker.class);
    private static final int MAX_RETRIES = 5;
    private final OutboxRepository outboxRepository;
    private final OutboxPublisher outboxPublisher;
    private final OutboxObservabilityService observabilityService;

    public OutboxWorker(OutboxRepository outboxRepository, OutboxPublisher outboxPublisher, OutboxObservabilityService observabilityService) {
        this.outboxRepository = outboxRepository;
        this.outboxPublisher = outboxPublisher;
        this.observabilityService = observabilityService;
    }

    @Scheduled(fixedDelayString = "${app.outbox.worker-delay-ms:5000}")
    public void publishPending() {
        outboxRepository.findPending(50).forEach(event -> {
            try {
                outboxRepository.markProcessing(event.eventId());
                outboxPublisher.publish(event);
                outboxRepository.markSent(event.eventId(), OffsetDateTime.now());
                observabilityService.recordPublishSuccess();
                LOG.info("outbox_event_published event_id={} aggregate_id={} event_type={}",
                        event.eventId(), event.aggregateId(), event.eventType());
            } catch (Exception ex) {
                int retries = (event.retryCount() == null ? 0 : event.retryCount()) + 1;
                OutboxEventStatus status = retries >= MAX_RETRIES ? OutboxEventStatus.FAILED : OutboxEventStatus.PENDING;
                outboxRepository.markRetry(event.eventId(), retries, status);
                observabilityService.recordPublishFailure();
                LOG.warn("outbox_event_publish_failed event_id={} aggregate_id={} event_type={} retry_count={} status={}",
                        event.eventId(), event.aggregateId(), event.eventType(), retries, status, ex);
            }
        });
    }

    @Scheduled(fixedDelayString = "${app.outbox.metrics-delay-ms:30000}")
    public void reportMetrics() {
        OutboxMetricsSnapshot metrics = observabilityService.snapshot();
        LOG.info("outbox_metrics pending_count={} retry_count={} failed_count={} publish_success_rate={}",
                metrics.pendingCount(),
                metrics.retryCount(),
                metrics.failedCount(),
                metrics.publishSuccessRate());
    }
}
