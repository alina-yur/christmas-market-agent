package com.example.tools;

import com.example.model.Market;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;
import java.util.Map;

@Serdeable
public record ChristmasMarketData(
                List<Market> markets,
                Map<String, Integer> baseCapacity,
                Map<String, Double> prices) {
}
