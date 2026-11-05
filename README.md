# ArbitraAI – Autonomous Deep Research LLM Agent

**ArbitraAI** is a high-performance, autonomous research engine built with **Java 21** and **Spring Boot 3.x**. It bridges enterprise Java backend architectures with agentic AI workflows, replacing manual web browsing with automated multi-source research, fact deduplication, real-time status streaming, and citation-backed report generation.

---

## 🌟 Key Features

* **Multi-Tier Execution Framework:**
  * **`NORMAL`**: Direct LLM synthesis for rapid, low-latency queries.
  * **`SMALL`**: Single-pass web retrieval for quick topic overviews with real-time context.
  * **`DEEP`**: Multi-pass autonomous research pipeline executing iterative query decomposition and web synthesis.
* **Non-Blocking Real-Time Streaming:** Streams step-by-step reasoning logs and execution updates via **Server-Sent Events (SSE)** and Java `CompletableFuture`.
* **Agentic Workflows & RAG Integration:** Powered by **LangChain4j** and **Tavily API** to scrape, filter, deduplicate, and synthesize web context into structured Markdown reports.
* **Session Persistence:** Full data persistence using **Spring Data JPA** and an embedded **H2 Database**, supporting session tracking and rapid search history retrieval.
* **Clean Decoupled Architecture:** Strict separation across Controller, Service, DTO, Repository, and Model layers.

---

## 🏗️ System Architecture

```text
               +-------------------------------------------------------+
               |                    User Interface                     |
               |                (HTML5 / CSS3 / JS)                    |
               +---------------------------+---------------------------+
                                           |
                                 POST /api/research
                                           |
                                           v
               +-------------------------------------------------------+
               |                  ResearchController                   |
               +---------------------------+---------------------------+
                                           |
                                  Async Processing
                               (CompletableFuture)
                                           |
                                           v
               +-------------------------------------------------------+
               |                 DeepResearchService                   |
               +-------------+---------------------------+-------------+
                             |                           |
                 Autonomous Tool Execution       Streaming Updates (SSE)
                             |                           |
                             v                           v
               +---------------------------+   +-----------------------+
               |    TavilySearchService    |   |   SseLoggingService   |
               |     (External API)        |   +-----------------------+
               +-------------+-------------+
                             |
                      Web Context Scraping
                             |
                             v
               +-------------------------------------------------------+
               |             LangChain4j LLM Orchestrator              |
               |          (Fact Deduplication & Synthesis)             |
               +---------------------------+---------------------------+
                                           |
                                    Save Report & State
                                           |
                                           v
               +-------------------------------------------------------+
               |  Spring Data JPA Repository  -->  H2 In-Memory DB     |
               +-------------------------------------------------------+
```


ArbitraAI/Backend
|--src/
|  |--Frontend
|  |  |--app.js
