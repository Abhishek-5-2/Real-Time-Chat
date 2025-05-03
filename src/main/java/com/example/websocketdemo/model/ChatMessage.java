package com.example.websocketdemo.model;
import java.security.Timestamp;
import java.time.Instant;
public class ChatMessage {

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        TYPING,
        STOP_TYPING
    }

    private MessageType type;
    private String content;
    private String sender;
    private String roomId;
    private String sessionId; // ✅ Add this field
    private Instant timestamp;
    // Getters and Setters
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public Instant getTimestamp(){
        return timestamp;
    }
    public void setTimestamp(Instant timestamp){
        this.timestamp = timestamp;
    }
}
