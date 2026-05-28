package com.github.melihemreguler.turknetmessagingservice.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Ensures MongoDB indexes for hot query paths at application startup.
 *
 * <p>Spring Data Mongo 3.0+ disables auto-index-creation by default, so the
 * {@code @Indexed} annotations on DTOs are advisory only. This class makes
 * index creation explicit and idempotent — safe to run on every startup
 * because {@code indexOps.createIndex} is a no-op if the index already exists
 * with the same definition.
 *
 * <p>Indexes covered:
 * <ul>
 *   <li>{@code messages}: compound {@code (threadId asc, timestamp desc)} —
 *       covers paginated thread history sort and the inbox aggregation
 *       {@code $match}+{@code $sort} prefix.</li>
 *   <li>{@code sessions}: TTL on {@code expiresAt} — MongoDB removes expired
 *       sessions automatically; the scheduled cleanup becomes a safety net.</li>
 *   <li>{@code activity_logs}: unique on {@code userId} — there is at most one
 *       log document per user (the consumer's upsert pattern depends on this).</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MongoIndexConfig {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void ensureIndexes() {
        // Each createIndex is wrapped — an IndexOptionsConflict on one collection
        // (e.g. a legacy non-unique index already present) shouldn't abort startup.
        tryEnsure("messages", this::ensureMessagesIndexes);
        tryEnsure("sessions", this::ensureSessionsIndexes);
        tryEnsure("activity_logs", this::ensureActivityLogsIndexes);
    }

    private void tryEnsure(String collection, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.warn("Index ensure failed for {} — existing index may conflict; drop manually if needed: {}",
                    collection, e.getMessage());
        }
    }

    private void ensureMessagesIndexes() {
        IndexOperations ops = mongoTemplate.indexOps("messages");
        // Powers findByThreadIdOrderByTimestampDesc + the inbox aggregation
        // (which sorts by timestamp desc within each threadId).
        String name = ops.createIndex(new Index()
                .on("threadId", Sort.Direction.ASC)
                .on("timestamp", Sort.Direction.DESC)
                .named("threadId_1_timestamp_-1"));
        log.info("Ensured index on messages: {}", name);
    }

    private void ensureSessionsIndexes() {
        IndexOperations ops = mongoTemplate.indexOps("sessions");
        // TTL: MongoDB will purge documents whose `expiresAt` <= now.
        // expireAfter(0s) means "expire at the value of the field itself".
        String name = ops.createIndex(new Index()
                .on("expiresAt", Sort.Direction.ASC)
                .expire(0, TimeUnit.SECONDS)
                .named("expiresAt_ttl"));
        log.info("Ensured TTL index on sessions: {}", name);
    }

    private void ensureActivityLogsIndexes() {
        IndexOperations ops = mongoTemplate.indexOps("activity_logs");
        // One activity log document per user. Unique guards against the race
        // condition where two consumer threads insert simultaneously when no
        // document exists yet.
        String name = ops.createIndex(new Index()
                .on("userId", Sort.Direction.ASC)
                .unique()
                .named("userId_unique"));
        log.info("Ensured unique index on activity_logs: {}", name);
    }
}
