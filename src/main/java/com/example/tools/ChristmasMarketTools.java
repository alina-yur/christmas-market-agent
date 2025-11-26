package com.example.tools;

import dev.langchain4j.agent.tool.Tool;
import com.example.model.Market;
import com.example.model.Reservation;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public class ChristmasMarketTools {

        private static final Logger LOG = LoggerFactory.getLogger(ChristmasMarketTools.class);
        private final ChristmasMarketDataProvider dataProvider;

        public ChristmasMarketTools(ChristmasMarketDataProvider dataProvider) {
                this.dataProvider = dataProvider;
        }

        @Tool("List Christmas markets for a given date (use current date if user doesn't specify). Returns markets with their offerings and available time slots.")
        public List<Market> listMarkets(LocalDate date) {
                LOG.info("AI requesting markets for date: {}", date != null ? date : "Today");
                return dataProvider.getData().markets();
        }

        @Tool("Reserve an offering at a market and slot. Returns CONFIRMED or REJECTED with price.")
        public Reservation reserve(String marketId,
                        String offering,
                        String slot,
                        int partySize,
                        String name) {
                LOG.info("Attempting reservation: {} @ {} for {} people ({})", offering, slot, partySize, name);

                Map<String, Integer> baseCapacity = dataProvider.getData().baseCapacity();

                String key = offering.toLowerCase() + ":" + slot.toLowerCase();
                int capacity = baseCapacity.getOrDefault(key, 10);

                if (partySize > capacity) {
                        return new Reservation(
                                        null, marketId, slot, partySize, name,
                                        0.0, "REJECTED", "Slot full for that party size");
                }

                Map<String, Double> prices = dataProvider.getData().prices();
                double perPerson = prices.getOrDefault(offering.toLowerCase(), dataProvider.getData().defaultPrice());

                double total = perPerson * partySize;
                String ref = "XM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                return new Reservation(
                                ref, marketId, slot, partySize, name,
                                total, "CONFIRMED", null);
        }
}
