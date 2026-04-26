package com.supervision.livraisons.controller;

import com.supervision.livraisons.model.ChatMessage;
import com.supervision.livraisons.service.LivraisonService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWsController {

    private final LivraisonService livraisonService;

    public ChatWsController(LivraisonService livraisonService) {
        this.livraisonService = livraisonService;
    }

    @MessageMapping("/chat/{nocde}")
    @SendTo("/topic/chat/{nocde}")
    public ChatMessage publish(@DestinationVariable Integer nocde, ChatMessage message) {
        // senderId should be set by the mobile client from JWT identity
        Integer senderId = message.getSenderId() != null ? message.getSenderId() : -1;
        // Assuming WebSocket sender is a livreur (P001)
        return livraisonService.postChatMessage(nocde, senderId, "P001", message);
    }
}
