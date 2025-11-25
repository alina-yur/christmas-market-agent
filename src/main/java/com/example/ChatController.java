package com.example;

import com.example.service.ChristmasMarketAssistant;

import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

@Controller("/api")
public class ChatController {
    private final ChristmasMarketAssistant assistant;

    public ChatController(ChristmasMarketAssistant assistant) {
        this.assistant = assistant;
    }

    @Serdeable
    public record ChatRequest(String message) {
    }

    @Post(uri = "/chat", consumes = MediaType.APPLICATION_JSON, produces = MediaType.TEXT_PLAIN)
    public String chat(@Body ChatRequest req) {
        return assistant.chat(req.message());
    }
}
