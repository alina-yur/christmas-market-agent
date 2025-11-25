package com.example.tools;

import dev.langchain4j.agent.tool.Tool;
import com.example.model.Market;
import com.example.model.Reservation;
import jakarta.inject.Singleton;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public class ChristmasMarketTools {

        private final ChristmasMarketDataProvider dataProvider;

        public ChristmasMarketTools(ChristmasMarketDataProvider dataProvider) {
                this.dataProvider = dataProvider;
        }

        @Tool("List Christmas markets for a given date (use current date if user doesn't specify). Returns markets with their offerings and available time slots.")
        public List<Market> listMarkets(LocalDate date) {
                return dataProvider.getData().markets();
        }

        @Tool("Reserve an offering at a market and slot. Returns CONFIRMED or REJECTED with price.")
        public Reservation reserve(String marketId,
                        String offering,
                        String slot,
                        int partySize,
                        String name) {

                Map<String, Integer> baseCapacity = dataProvider.getData().baseCapacity();

                String key = offering.toLowerCase() + ":" + slot.toLowerCase();
                int capacity = baseCapacity.getOrDefault(key, 10);

                if (partySize > capacity) {
                        return new Reservation(
                                        null, marketId, slot, partySize, name,
                                        0.0, "REJECTED", "Slot full for that party size");
                }

                Map<String, Double> prices = dataProvider.getData().prices();
                double perPerson = prices.getOrDefault(offering.toLowerCase(), 20.0);

                double total = perPerson * partySize;
                String ref = "XM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                return new Reservation(
                                ref, marketId, slot, partySize, name,
                                total, "CONFIRMED", null);
        }
}
