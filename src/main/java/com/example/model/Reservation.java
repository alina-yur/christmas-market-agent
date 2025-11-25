package com.example.model;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Reservation(
                String reference,
                String marketId,
                String slot,
                int partySize,
                String name,
                double totalCHF,
                String status,
                String reason) {
}