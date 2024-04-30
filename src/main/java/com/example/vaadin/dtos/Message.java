package com.example.vaadin.dtos;

import java.time.Instant;

public class Message {

    private String from;

    private String text;

    private String to;

    private Instant time;

    public Message(String from, String text, String to) {
        this.from = from;
        this.text = text;
        this.to = to;
    }

    public Instant getTime() {
        return this.time;
    }

    public Instant time() {
        this.time = Instant.now();
        return time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
