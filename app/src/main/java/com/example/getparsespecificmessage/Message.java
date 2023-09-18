package com.example.getparsespecificmessage;

public class Message {
    private String sender;
    private String body;
    private long timestamp;

    public Message(String sender, String body, long timestamp) {
        this.sender = sender;
        this.body = body;
        this.timestamp = timestamp;
    }

    // Ajoutez les getters et les setters si nécessaire


    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
