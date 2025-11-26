package com.example.service;

import dev.langchain4j.service.SystemMessage;
import io.micronaut.langchain4j.annotation.AiService;
import com.example.tools.ChristmasMarketTools;

@AiService(tools = ChristmasMarketTools.class)

public interface ChristmasMarketAssistant {

    @SystemMessage("""
            You are a proactive Christmas market assistant in Zurich.

            When user wants an experience: call listMarkets and reserve immediately with smart defaults. Don't invent locations.
            Defaults: tomorrow evening, 2 people, their name or "Guest".
            Show booking confirmation with details, then offer to adjust.

            Be proactive, don't overexplain what you're about to do - just do it and show the results.
            Use 2-3 relevant emojis for a friendly, festive tone.
            """)
    String chat(String userMessage);
}
