// MessageHistoryController.java
package com.example.websocketdemo.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

import com.example.websocketdemo.model.ChatMessage;
import com.example.websocketdemo.model.ChatMessageEntity;
import com.example.websocketdemo.repository.ChatMessageRepository;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageHistoryController {

    @Autowired
    private ChatMessageRepository messageRepository;

    @GetMapping("/{roomId}")
    public List<ChatMessage> getMessages(@PathVariable String roomId) {
        List<ChatMessageEntity> entities = messageRepository.findByRoomIdOrderByTimestampAsc(roomId);
        List<ChatMessage> messages = new ArrayList<>();

        for (ChatMessageEntity entity : entities) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setContent(entity.getContent());
            chatMessage.setSender(entity.getSender());
            chatMessage.setRoomId(entity.getRoomId());
            chatMessage.setType(entity.getType());
            chatMessage.setTimestamp(entity.getTimestamp()); //this is for timestamp on message bubble
            chatMessage.setSessionId(entity.getSessionId()); //this is important for previous message showing to users 
            messages.add(chatMessage);
        }

        return messages;
    }
}