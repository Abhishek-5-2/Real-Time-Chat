package com.example.websocketdemo.repository;

import com.example.websocketdemo.model.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findByRoomId(String roomId);

List<ChatMessageEntity> findByRoomIdOrderByTimestampAsc(String roomId);
}