package com.example.vaadin.services;

import com.example.vaadin.dtos.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class ChatService {

    private final Sinks.Many<Message> messages = Sinks.many().multicast().directBestEffort();

    private final Flux<Message> messagesFlux = messages.asFlux();

    public void add(String from, String text, String to) {
        this.messages.tryEmitNext(new Message(from, text, to));
    }

    public Flux<Message> join() {
        return this.messagesFlux;
    }
}
