package com.example.websocketdemo.controller;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.websocketdemo.model.ChatMessage;
import com.example.websocketdemo.model.ChatMessageEntity;
import com.example.websocketdemo.repository.ChatMessageRepository;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository messageRepository;

    private final ConcurrentHashMap<String, Set<String>> roomUsers = new ConcurrentHashMap<>();

    @Autowired
    public ChatController(SimpMessagingTemplate messagingTemplate,
                          ChatMessageRepository messageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
    }
    

    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@DestinationVariable String roomId,
                            @Payload ChatMessage chatMessage) {

        chatMessage.setRoomId(roomId);
        System.out.println("✅ Received CHAT message in room " + roomId + ": " + chatMessage.getContent());

        // Save to DB
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setSender(chatMessage.getSender());
        entity.setContent(chatMessage.getContent());
        entity.setRoomId(roomId);
        entity.setTimestamp(Instant.now());
        entity.setSessionId(chatMessage.getSessionId());// to display previous messages to new users with senders name and profile
        entity.setType(chatMessage.getType());
        messageRepository.save(entity);

        chatMessage.setTimestamp(entity.getTimestamp());

        messagingTemplate.convertAndSend("/topic/messages/" + roomId, chatMessage);
       
    }

    @MessageMapping("/chat/{roomId}/typing")
    public void typing(@DestinationVariable String roomId, @Payload ChatMessage message) {
        messagingTemplate.convertAndSend("/topic/typing/" + roomId, message);
    }

    @MessageMapping("/chat/{roomId}/addUser")
    public void addUser(@DestinationVariable String roomId,
                        @Payload ChatMessage chatMessage,
                        SimpMessageHeaderAccessor headerAccessor) {

        if (chatMessage.getType() == ChatMessage.MessageType.JOIN) {
            headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
            roomUsers.computeIfAbsent(roomId, k -> new HashSet<>()).add(chatMessage.getSender());

            chatMessage.setRoomId(roomId);
            messagingTemplate.convertAndSend("/topic/messages/" + roomId, chatMessage);
            updateOnlineUsers(roomId);
        }
    }

    @MessageMapping("/chat/{roomId}/removeUser")
    public void removeUser(@DestinationVariable String roomId,
                           @Payload ChatMessage chatMessage,
                           SimpMessageHeaderAccessor headerAccessor) {

        if (chatMessage.getType() == ChatMessage.MessageType.LEAVE) {
            Set<String> usersInRoom = roomUsers.get(roomId);
            if (usersInRoom != null) {
                usersInRoom.remove(chatMessage.getSender());
            }

            chatMessage.setRoomId(roomId);
            messagingTemplate.convertAndSend("/topic/messages/" + roomId, chatMessage);
            updateOnlineUsers(roomId);
        }
    }

    @MessageMapping("/chat/{roomId}/history")
    public void getMessageHistory(@DestinationVariable String roomId,
                                  SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();

        List<ChatMessageEntity> entities = messageRepository.findByRoomId(roomId);

        entities.forEach(entity -> {
            ChatMessage message = new ChatMessage();
            message.setSender(entity.getSender());
            message.setContent(entity.getContent());
            message.setRoomId(entity.getRoomId());
            message.setTimestamp(entity.getTimestamp());
            message.setSessionId(entity.getSessionId());
            message.setType(entity.getType() != null ? entity.getType() : ChatMessage.MessageType.CHAT);
            
      messagingTemplate.convertAndSendToUser(headerAccessor.getUser().getName(), "/queue/history", message);
        });
    }

    private void updateOnlineUsers(String roomId) {
        Set<String> usersInRoom = roomUsers.get(roomId);
        if (usersInRoom != null) {
            messagingTemplate.convertAndSend("/topic/onlineUsers/" + roomId, usersInRoom);
        }
    }
}