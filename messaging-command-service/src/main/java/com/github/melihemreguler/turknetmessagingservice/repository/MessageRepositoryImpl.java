package com.github.melihemreguler.turknetmessagingservice.repository;

import com.github.melihemreguler.turknetmessagingservice.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepositoryCustom {

    static final String COLLECTION = "messages";

    private final MongoTemplate mongoTemplate;

    @Override
    public List<MessageDto> findLatestPerThreadForUser(String userId, int limit, int offset) {
        Criteria match = buildThreadCriteria(userId);

        // Raw $group with $$ROOT — spring-data's GroupOperation.first(String) would
        // re-interpret "$$ROOT" as a field reference, so build the stage by hand.
        AggregationOperation groupLatest = ctx -> new Document(
                "$group",
                new Document("_id", "$threadId")
                        .append("latest", new Document("$first", "$$ROOT"))
        );
        AggregationOperation replaceRoot = ctx -> new Document(
                "$replaceRoot", new Document("newRoot", "$latest")
        );

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(match),
                Aggregation.sort(Sort.Direction.DESC, "timestamp"),
                groupLatest,
                replaceRoot,
                Aggregation.sort(Sort.Direction.DESC, "timestamp"),
                Aggregation.skip((long) offset),
                Aggregation.limit(limit)
        );

        AggregationResults<MessageDto> results = mongoTemplate.aggregate(
                aggregation, COLLECTION, MessageDto.class);
        return results.getMappedResults();
    }

    @Override
    public long countThreadsForUser(String userId) {
        Criteria match = buildThreadCriteria(userId);
        return mongoTemplate.getCollection(COLLECTION)
                .distinct("threadId", new Query(match).getQueryObject(), String.class)
                .into(new ArrayList<>())
                .size();
    }

    /**
     * threadId is `<minId>-<maxId>` (lex). Match threads containing this user
     * either as prefix (`userId-...`) or suffix (`...-userId`).
     */
    private Criteria buildThreadCriteria(String userId) {
        String quoted = Pattern.quote(userId);
        Pattern threadPattern = Pattern.compile("^" + quoted + "-|-" + quoted + "$");
        return Criteria.where("threadId").regex(threadPattern);
    }
}
