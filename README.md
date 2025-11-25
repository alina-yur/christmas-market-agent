# Christmas Market Booking Agent

A conversational AI assistant for booking Christmas market experiences in Zurich. Built with Micronaut and LangChain4j to show how you can wire up an LLM with tools and let it actually do things.

## About This Project

Chat with an AI assistant that searches Christmas markets and makes reservations. No forms, no clicking through menus. You say what you want, the AI calls Java methods to list markets and book slots, then tells you what it did. Conversation context is preserved, so you can adjust your booking without repeating yourself.

## Core Concepts

### Micronaut

Micronaut is a JVM framework built around compile-time processing. Instead of runtime reflection, it uses annotation processors to generate dependency injection code at build time. You get fast startup low memory usage, and GraalVM Native Image compatibility from the ground up. Configuration happens through standard properties files, and you can inject beans as usual.

### LangChain4j

LangChain4j wraps the complexity of working with LLMs into Java abstractions. It handles prompt construction, API calls to providers like OpenAI, response parsing, and what's pretty cool things like chat memory (for stateful conversations). You can then work with interfaces and annotations.

### Micronaut LangChain4j Integration

The [integration](https://micronaut-projects.github.io/micronaut-langchain4j/latest/guide/) uses Micronaut's annotation processing to generate LangChain4j service implementations at compile time. You write an interface with `@AiService`, and the framework generates everything needed to call the LLM, manage tools, and handle responses. Configuration goes in `application.properties`.

### AI Services (@AiService)

An AI Service is an interface where you define methods that interact with an LLM. The Micronaut processor generates the implementation. You can call a method with a string, that will be sent to the LLM with your configuration to get a response.

### System Messages (@SystemMessage)

System messages set the AI's behavior and persona. They're sent with every request to establish context about role, personality, and response format. In this project, the system message tells the assistant to be proactive, make reservations immediately using smart defaults, and use a friendly tone.

### Tools (@Tool)

Tools allow predefening certain actions in Java. You can annotate methods with `@Tool` and provide descriptions. When the AI decides it needs data or needs to perform an action, it requests tool execution. The framework calls your method with parameters the AI provides, and returns the result. In this project, the tools are used to search markets and books reservations.

### Chat Memory

Chat memory keeps conversation history stateful, so the AI can reference what was said before. Previous messages are included in subsequent requests for better user experience.
## Project Structure

- `XmasMarketAssistant` - AI service interface defining the chat method
- `XmasMarketTools` - Tool provider with methods for listing markets and making reservations
- `ChatController` - REST endpoint exposing the AI assistant via HTTP
- `XmasMarketDataProvider` - Loads market data from JSON configuration
- `Market` and `Reservation` - Data models for markets and bookings

## Running the Application

```bash
mvn mn:run
```

The assistant will be available at `http://localhost:8080/api/chat`.

## Running as GraalVM Native Image

```bash
mvn package -Dpackaging=native-image
./target/christmas-market-agent
[main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 19ms. Server Running: http://localhost:8080
```