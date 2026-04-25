package com.supervision.livraisons.model;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {
    @SerializedName("id")
    private long id;
    @SerializedName("nocde")
    private int nocde;
    @SerializedName("senderId")
    private int senderId;
    @SerializedName("recipientId")
    private Integer recipientId;
    @SerializedName("messageText")
    private String messageText;
    @SerializedName("sentAt")
    private String sentAt;

    public ChatMessage() {}

    public ChatMessage(String messageText) {
        this.messageText = messageText;
    }

    public long getId() { return id; }
    public int getNocde() { return nocde; }
    public int getSenderId() { return senderId; }
    public Integer getRecipientId() { return recipientId; }
    public String getMessageText() { return messageText; }
    public String getSentAt() { return sentAt; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
}
