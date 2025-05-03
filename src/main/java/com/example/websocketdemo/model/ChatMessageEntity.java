// ChatMessageEntity.java
package com.example.websocketdemo.model;

import javax.persistence.*;

import com.example.websocketdemo.model.ChatMessage.MessageType;

import java.time.Instant;

@Entity
@Table(name = "chat_messages")
public class ChatMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String content;
    private String roomId;
    private Instant timestamp;

    private String sessionId;

    // Getters
    public Long getId() { return id; }
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public String getRoomId() { return roomId; }
    public Instant getTimestamp() { return timestamp; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setSender(String sender) { this.sender = sender; }
    public void setContent(String content) { this.content = content; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    private MessageType type; 

public MessageType getType() {
    return type;
}

public void setType(MessageType type) {
    this.type = type;
}

public String getSessionId() {
    return sessionId;
}
public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
}

}

