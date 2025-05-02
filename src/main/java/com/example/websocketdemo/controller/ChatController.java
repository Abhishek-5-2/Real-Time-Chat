package com.example.websocketdemo.controller;

import java.util.HashSet;
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

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    
    // A map to store active users per room
    private final ConcurrentHashMap<String, Set<String>> roomUsers = new ConcurrentHashMap<>();

    @Autowired
    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@DestinationVariable String roomId,
                            @Payload ChatMessage chatMessage) {
        chatMessage.setRoomId(roomId);
        System.out.println("✅ Received CHAT message in room " + roomId + ": " + chatMessage.getContent());

        messagingTemplate.convertAndSend("/topic/messages/" + roomId, chatMessage);
    }

    @MessageMapping("/chat/{roomId}/typing")
    public void typing(@DestinationVariable String roomId, @Payload ChatMessage message) {
        // Send typing event to everyone in the room
        messagingTemplate.convertAndSend("/topic/typing/" + roomId, message);
    }

    @MessageMapping("/chat/{roomId}/addUser")
    public void addUser(@DestinationVariable String roomId,
                        @Payload ChatMessage chatMessage,
                        SimpMessageHeaderAccessor headerAccessor) {

        if (chatMessage.getType() == ChatMessage.MessageType.JOIN) {
            // Store username in session attributes for presence tracking
            headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());

            // Add the user to the active users list for the room
            roomUsers.computeIfAbsent(roomId, k -> new HashSet<>()).add(chatMessage.getSender());

            // Notify all users in the room about the new user
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
            // Remove the user from the active users list for the room
            Set<String> usersInRoom = roomUsers.get(roomId);
            if (usersInRoom != null) {
                usersInRoom.remove(chatMessage.getSender());
            }

            // Notify all users in the room about the removed user
            chatMessage.setRoomId(roomId);
            messagingTemplate.convertAndSend("/topic/messages/" + roomId, chatMessage);
            updateOnlineUsers(roomId);
        }
    }

    // Helper method to broadcast the list of online users in the room
    private void updateOnlineUsers(String roomId) {
        Set<String> usersInRoom = roomUsers.get(roomId);
        if (usersInRoom != null) {
            // Send the list of users to everyone in the room
            messagingTemplate.convertAndSend("/topic/onlineUsers/" + roomId, usersInRoom);
        }
    }
}
