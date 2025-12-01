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

        @Tool("""
                        List available Christmas markets with their offerings and time slots.
                        Returns a list of markets, each with: id, name, offerings (e.g. 'fondue', 'gluehwein'), and slots (e.g. 'morning', 'afternoon', 'evening').
                        You MUST use the exact market IDs and offering names returned by this tool when making reservations.
                        """)
        public List<Market> listMarkets(LocalDate date) {

                LOG.info("Calling tool: listMarkets (date={})", date != null ? date : "today");
                List<Market> markets = dataProvider.getData().markets();
                return markets;
        }

        @Tool("""
                        Reserve an offering at a specific market for a given time slot.
                        Parameters:
                        - marketId: Use exact market ID from listMarkets (e.g. 'm1', 'm2')
                        - offering: Use exact offering name from listMarkets (e.g. 'fondue', 'gluehwein', 'raclette')
                        - slot: Use exact slot name from listMarkets (e.g. 'morning', 'afternoon', 'evening')
                        - partySize: Number of people
                        - name: Guest name for the reservation
                        Returns a Reservation with status CONFIRMED or REJECTED, including price and reference number.
                        """)
        public Reservation reserve(String marketId, String offering, String slot, int partySize, String name) {
                LOG.info("Calling tool: reserve (market={}, offering={}, slot={}, guests={}, name={})",
                                marketId, offering, slot, partySize, name);

                Market market = dataProvider.getData().markets().stream()
                                .filter(m -> m.id().equals(marketId))
                                .findFirst()
                                .orElse(null);

                if (market == null) {
                        return reject(marketId, null, slot, partySize, name,
                                        "Invalid marketId '" + marketId + "'. Use exact ID from listMarkets.");
                }

                boolean validOffering = market.offerings().stream()
                                .anyMatch(o -> o.equalsIgnoreCase(offering));
                if (!validOffering) {
                        return reject(marketId, market.name(), slot, partySize, name,
                                        "Invalid offering '" + offering + "'. Use exact offering from listMarkets.");
                }

                boolean validSlot = market.slots().stream()
                                .anyMatch(s -> s.equalsIgnoreCase(slot));
                if (!validSlot) {
                        return reject(marketId, market.name(), slot, partySize, name,
                                        "Invalid slot '" + slot + "'. Use exact slot from listMarkets.");
                }

                String key = offering.toLowerCase() + ":" + slot.toLowerCase();
                Integer maxCapacity = dataProvider.getData().baseCapacity().get(key);
                Double pricePerPerson = dataProvider.getData().prices().get(offering.toLowerCase());

                if (maxCapacity == null || pricePerPerson == null) {
                        return reject(marketId, market.name(), slot, partySize, name, "Item or slot not available");
                }

                int currentLoad = currentBookings.getOrDefault(key, 0);
                if (currentLoad + partySize > maxCapacity) {
                        LOG.warn("Reservation rejected: Capacity exceeded ({} + {} > {})", currentLoad, partySize,
                                        maxCapacity);
                        return reject(marketId, market.name(), slot, partySize, name,
                                        "Slot full. Remaining capacity: " + (maxCapacity - currentLoad));
                }

                currentBookings.put(key, currentLoad + partySize);
                double total = pricePerPerson * partySize;
                String ref = "XM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                LOG.info("Reservation confirmed: {} (CHF {})", ref, total);
                return new Reservation(ref, marketId, market.name(), slot, partySize, name, total, "CONFIRMED", null);
        }

        private Reservation reject(String marketId, String marketName, String slot, int size, String name, String reason) {
                return new Reservation(null, marketId, marketName, slot, size, name, 0.0, "REJECTED", reason);
        }
}