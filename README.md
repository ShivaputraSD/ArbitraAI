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
## 📁 Repository Structure

```text

ArbitraAI/
├── src/
│   ├── main/
│   │   ├── java/Shivu/ArbitraAI/
│   │   │   ├── config/
│   │   │   │   └── AgentConfig.java
│   │   │   ├── controller/
│   │   │   │   └── ResearchController.java
│   │   │   ├── dto/
│   │   │   │   ├── ResearchMode.java
│   │   │   │   ├── ResearchRequest.java
│   │   │   │   ├── ResearchResponse.java
│   │   │   │   └── SearchResult.java
│   │   │   ├── model/
│   │   │   │   └── ResearchState.java
│   │   │   ├── repository/
│   │   │   │   └── ResearchReportRepository.java
│   │   │   ├── service/
│   │   │   │   ├── DeepResearchService.java
│   │   │   │   └── TavlySearchService.java
│   │   │   └── ArbitraAiApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── static/
│   │           ├── index.html
│   │           ├── style.css
│   │           └── app.js
│   └── test/
└── pom.xml

```

## 🛠️ Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.x
* **AI & Orchestration:** LangChain4j, Tavily Search API
* **Database & Persistence:** Spring Data JPA, H2 Database
* **Concurrency & Communication:** Java CompletableFuture, Server-Sent Events (SSE), RESTful APIs
* **Frontend:** HTML5, Modern CSS3, Vanilla JavaScript, Marked.js


## 🚀 Getting Started

**Prerequisites**
* **JDK 21** or higher installed
* **Maven 3.8+** installed
* **Tavily API Key and OpenAI/Gemini API Key**

**1. Clone the Repository**
```text
https://github.com/ShivaputraSD/ArbitraAI.git
```
**2. Configure Environment Variables**

Create or Update application.yml with your configuration and API keys:

```text
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:arbitradb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  h2:
    console:
      enabled: true

api:
  tavily:
    key: ${TAVILY_API_KEY}
  llm:
    key: ${LLM_API_KEY}
```

**3.Build & Run**

```text
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

**4.Access the Platform**

Open your browser and navigate to:

```text
http://localhost:8080
```

