package com.example.tools;

import io.micronaut.serde.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;

@Singleton
public class ChristmasMarketDataProvider {

    private final ObjectMapper objectMapper;
    private ChristmasMarketData data;

    public ChristmasMarketDataProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void load() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("christmas-markets.json")) {
            if (is == null) {
                throw new IllegalStateException("Resource christmas-markets.json not found");
            }
            this.data = objectMapper.readValue(is, ChristmasMarketData.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load christmas-markets.json", e);
        }
    }

    public ChristmasMarketData getData() {
        return data;
    }
}
