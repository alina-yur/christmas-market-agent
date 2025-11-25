package com.example.model;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public record Market(
        String id,
        String name,
        List<String> offerings,
        List<String> slots) {
}