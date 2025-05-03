package com.example.websocketdemo.repository;

import java.time.Instant;

import com.example.websocketdemo.model.ChatMessageEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findByRoomId(String roomId);

    List<ChatMessageEntity> findByRoomIdOrderByTimestampAsc(String roomId);

    @Query("SELECT m FROM ChatMessageEntity m WHERE m.roomId = :roomId AND m.timestamp >= :fromTime ORDER BY m.timestamp ASC")
    List<ChatMessageEntity> findMessagesSince(
        @Param("roomId") String roomId,
        @Param("fromTime") Instant fromTime
    );
}