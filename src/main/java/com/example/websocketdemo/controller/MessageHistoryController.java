package com.example.websocketdemo.controller;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.websocketdemo.model.ChatMessage;
import com.example.websocketdemo.model.ChatMessageEntity;
import com.example.websocketdemo.repository.ChatMessageRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;



@RestController
@RequestMapping("/api/messages")
public class MessageHistoryController {

    @Autowired
    private ChatMessageRepository messageRepository;

    @GetMapping("/{roomId}")
    public List<ChatMessage> getMessages(
        @PathVariable String roomId,
        @RequestParam(defaultValue = "1") int hours // fetch messages from last X hours
    ) {
        Instant fromTime = Instant.now().minus(Duration.ofHours(hours));
        List<ChatMessageEntity> entities = messageRepository.findMessagesSince(roomId, fromTime);
        List<ChatMessage> messages = new ArrayList<>();

        for (ChatMessageEntity entity : entities) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setContent(entity.getContent());
            chatMessage.setSender(entity.getSender());
            chatMessage.setRoomId(entity.getRoomId());
            chatMessage.setType(entity.getType());
            chatMessage.setTimestamp(entity.getTimestamp());
            chatMessage.setSessionId(entity.getSessionId());
            messages.add(chatMessage);
        }

        return messages;
    }
}