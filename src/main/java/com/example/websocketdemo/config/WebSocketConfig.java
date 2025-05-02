package com.example.websocketdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow cross-origin for development
                .withSockJS();                 // Fallback support for older browsers
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix for client-to-server messages (e.g., /app/chat/{roomId})
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix for server-to-client messages (e.g., /topic/messages/{roomId})
        registry.enableSimpleBroker("/topic","/queue");// Enable the message broker for destinations starting with /topic

        // For full-featured broker support like RabbitMQ or ActiveMQ, use the block below:
        /*
        registry.enableStompBrokerRelay("/topic")
                .setRelayHost("localhost")
                .setRelayPort(61613)
                .setClientLogin("guest")
                .setClientPasscode("guest");
        */
    }
}