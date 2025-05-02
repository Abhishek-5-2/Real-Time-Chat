package com.example.websocketdemo.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.websocketdemo.model.ChatMessage;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

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
    @SendTo("/topic/messages/{roomId}")
    public ChatMessage addUser(@DestinationVariable String roomId,
                               @Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {

        if (chatMessage.getType() == ChatMessage.MessageType.JOIN) {
            // Store username in session attributes for presence tracking
            headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        }

        chatMessage.setRoomId(roomId);  // Ensure roomId is set
        return chatMessage;
    }
}
