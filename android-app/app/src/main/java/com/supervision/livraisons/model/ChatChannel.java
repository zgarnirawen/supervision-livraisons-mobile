package com.supervision.livraisons.model;

import java.io.Serializable;

public class ChatChannel implements Serializable {
    private Integer nocde;
    private String title;
    private String lastMessage;
    private String lastMessageAt;
    private boolean isSupport;

    public Integer getNocde() { return nocde; }
    public String getTitle() { return title; }
    public String getLastMessage() { return lastMessage; }
    public String getLastMessageAt() { return lastMessageAt; }
    public boolean isSupport() { return isSupport; }
}
