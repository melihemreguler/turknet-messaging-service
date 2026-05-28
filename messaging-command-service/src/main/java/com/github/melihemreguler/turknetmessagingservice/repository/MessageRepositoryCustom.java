package com.github.melihemreguler.turknetmessagingservice.repository;

import com.github.melihemreguler.turknetmessagingservice.dto.MessageDto;

import java.util.List;

public interface MessageRepositoryCustom {

    /**
     * Returns the latest message of each thread the given user participates in,
     * sorted by recency (newest first), paginated.
     */
    List<MessageDto> findLatestPerThreadForUser(String userId, int limit, int offset);

    /**
     * Counts distinct threads the given user participates in.
     */
    long countThreadsForUser(String userId);
}
