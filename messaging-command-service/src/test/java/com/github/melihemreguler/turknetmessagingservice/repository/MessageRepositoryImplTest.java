package com.github.melihemreguler.turknetmessagingservice.repository;

import com.github.melihemreguler.turknetmessagingservice.dto.MessageDto;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    private MessageRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new MessageRepositoryImpl(mongoTemplate);
    }

    @Test
    void findLatestPerThreadForUser_buildsExpectedPipelineAndDelegatesToTemplate() {
        // Given
        String userId = "abc123";
        MessageDto returned = new MessageDto("abc123-zzz", "abc123", "alice", "hi");
        @SuppressWarnings("unchecked")
        AggregationResults<MessageDto> agg = (AggregationResults<MessageDto>) mock(AggregationResults.class);
        when(agg.getMappedResults()).thenReturn(List.of(returned));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("messages"), eq(MessageDto.class)))
                .thenReturn(agg);

        // When
        List<MessageDto> result = repository.findLatestPerThreadForUser(userId, 5, 10);

        // Then — result is passed through
        assertEquals(1, result.size());
        assertEquals(returned, result.get(0));

        // And — the pipeline is shaped correctly
        ArgumentCaptor<Aggregation> captor = ArgumentCaptor.forClass(Aggregation.class);
        verify(mongoTemplate).aggregate(captor.capture(), eq("messages"), eq(MessageDto.class));
        List<Document> stages = captor.getValue().toPipeline(Aggregation.DEFAULT_CONTEXT);

        // $match with regex containing userId
        Document matchStage = findStage(stages, "$match");
        assertNotNull(matchStage, "Expected $match stage");
        Document matchBody = (Document) matchStage.get("$match");
        Object threadId = matchBody.get("threadId");
        assertTrue(threadId.toString().contains(userId),
                "Expected $match regex to mention userId, got: " + threadId);

        // $group with $$ROOT
        Document groupStage = findStage(stages, "$group");
        assertNotNull(groupStage, "Expected $group stage");
        Document groupBody = (Document) groupStage.get("$group");
        assertEquals("$threadId", groupBody.get("_id"));
        Document latest = (Document) groupBody.get("latest");
        assertEquals("$$ROOT", latest.get("$first"));

        // $replaceRoot
        Document replaceRootStage = findStage(stages, "$replaceRoot");
        assertNotNull(replaceRootStage);
        assertEquals("$latest", ((Document) replaceRootStage.get("$replaceRoot")).get("newRoot"));

        // $skip / $limit reflect the args
        Document skipStage = findStage(stages, "$skip");
        assertNotNull(skipStage);
        assertEquals(10L, ((Number) skipStage.get("$skip")).longValue());
        Document limitStage = findStage(stages, "$limit");
        assertNotNull(limitStage);
        assertEquals(5L, ((Number) limitStage.get("$limit")).longValue());
    }

    @Test
    void findLatestPerThreadForUser_includesSortByTimestampDesc() {
        // Given
        @SuppressWarnings("unchecked")
        AggregationResults<MessageDto> agg = (AggregationResults<MessageDto>) mock(AggregationResults.class);
        when(agg.getMappedResults()).thenReturn(List.of());
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("messages"), eq(MessageDto.class)))
                .thenReturn(agg);

        // When
        repository.findLatestPerThreadForUser("u1", 10, 0);

        // Then
        ArgumentCaptor<Aggregation> captor = ArgumentCaptor.forClass(Aggregation.class);
        verify(mongoTemplate).aggregate(captor.capture(), eq("messages"), eq(MessageDto.class));
        List<Document> stages = captor.getValue().toPipeline(Aggregation.DEFAULT_CONTEXT);

        Document sortStage = findStage(stages, "$sort");
        assertNotNull(sortStage, "Expected at least one $sort stage");
        Document sortBody = (Document) sortStage.get("$sort");
        assertEquals(-1, ((Number) sortBody.get("timestamp")).intValue());
    }

    @Test
    void countThreadsForUser_returnsDistinctThreadIdCount() {
        // Given
        @SuppressWarnings("unchecked")
        MongoCollection<Document> collection = (MongoCollection<Document>) mock(MongoCollection.class);
        @SuppressWarnings("unchecked")
        DistinctIterable<String> distinct = (DistinctIterable<String>) mock(DistinctIterable.class);
        when(mongoTemplate.getCollection("messages")).thenReturn(collection);
        when(collection.distinct(eq("threadId"), any(Bson.class), eq(String.class))).thenReturn(distinct);
        // .into(new ArrayList<>()) — capture the list arg and stub return as a populated list
        when(distinct.into(any())).thenAnswer(inv -> {
            ArrayList<String> target = inv.getArgument(0);
            target.add("u1-u2");
            target.add("u1-u3");
            target.add("u1-u4");
            return target;
        });

        // When
        long result = repository.countThreadsForUser("u1");

        // Then
        assertEquals(3L, result);
    }

    @Test
    void countThreadsForUser_zeroWhenNoThreads() {
        // Given
        @SuppressWarnings("unchecked")
        MongoCollection<Document> collection = (MongoCollection<Document>) mock(MongoCollection.class);
        @SuppressWarnings("unchecked")
        DistinctIterable<String> distinct = (DistinctIterable<String>) mock(DistinctIterable.class);
        when(mongoTemplate.getCollection("messages")).thenReturn(collection);
        when(collection.distinct(eq("threadId"), any(Bson.class), eq(String.class))).thenReturn(distinct);
        when(distinct.into(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        long result = repository.countThreadsForUser("u1");

        // Then
        assertEquals(0L, result);
    }

    private static Document findStage(List<Document> stages, String op) {
        for (Document stage : stages) {
            if (stage.containsKey(op)) return stage;
        }
        return null;
    }
}
