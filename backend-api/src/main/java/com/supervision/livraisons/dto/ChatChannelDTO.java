package com.supervision.livraisons.dto;

import java.time.LocalDateTime;

public class ChatChannelDTO {
    private Integer nocde;
    private String title;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private boolean isSupport;

    public ChatChannelDTO(Integer nocde, String title, String lastMessage, LocalDateTime lastMessageAt, boolean isSupport) {
        this.nocde = nocde;
        this.title = title;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.isSupport = isSupport;
    }

    public Integer getNocde() { return nocde; }
    public String getTitle() { return title; }
    public String getLastMessage() { return lastMessage; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public boolean isSupport() { return isSupport; }
}
