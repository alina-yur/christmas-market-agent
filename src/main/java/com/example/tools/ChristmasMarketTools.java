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
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ChristmasMarketTools {

        private static final Logger LOG = LoggerFactory.getLogger(ChristmasMarketTools.class);

        private final Map<String, Integer> currentBookings = new ConcurrentHashMap<>();

        private final ChristmasMarketDataProvider dataProvider;

        public ChristmasMarketTools(ChristmasMarketDataProvider dataProvider) {
                this.dataProvider = dataProvider;
        }

        @Tool("List Christmas markets for a given date. Returns markets with their offerings and available time slots.")
        public List<Market> listMarkets(LocalDate date) {

                LOG.info("AI requesting markets for date: {}", date != null ? date : "Today");
                return dataProvider.getData().markets();
        }

        @Tool("Reserve an offering at a market and slot. Returns CONFIRMED or REJECTED with price.")
        public Reservation reserve(String marketId, String offering, String slot, int partySize, String name) {
                LOG.info("Attempting reservation: {} @ {} for {} people ({})", offering, slot, partySize, name);

                String key = offering.toLowerCase() + ":" + slot.toLowerCase();
                Integer maxCapacity = dataProvider.getData().baseCapacity().get(key);
                Double pricePerPerson = dataProvider.getData().prices().get(offering.toLowerCase());

                if (maxCapacity == null || pricePerPerson == null) {
                        return reject(marketId, slot, partySize, name, "Item or slot not available");
                }

                int currentLoad = currentBookings.getOrDefault(key, 0);
                if (currentLoad + partySize > maxCapacity) {
                        LOG.warn("Reservation rejected: Capacity exceeded ({} + {} > {})", currentLoad, partySize,
                                        maxCapacity);
                        return reject(marketId, slot, partySize, name,
                                        "Slot full. Remaining capacity: " + (maxCapacity - currentLoad));
                }

                currentBookings.put(key, currentLoad + partySize);
                double total = pricePerPerson * partySize;
                String ref = "XM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                LOG.info("Reservation confirmed: {}", ref);
                return new Reservation(ref, marketId, slot, partySize, name, total, "CONFIRMED", null);
        }

        private Reservation reject(String marketId, String slot, int size, String name, String reason) {
                return new Reservation(null, marketId, slot, size, name, 0.0, "REJECTED", reason);
        }
}