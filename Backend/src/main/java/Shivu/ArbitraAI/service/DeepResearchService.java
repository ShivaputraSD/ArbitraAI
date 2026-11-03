package Shivu.ArbitraAI.service;

import Shivu.ArbitraAI.dto.ResearchMode;
import Shivu.ArbitraAI.dto.ResearchResponse;
import Shivu.ArbitraAI.dto.SearchResult;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class DeepResearchService {

    private final ChatLanguageModel chatModel;
    private final TavlySearchService searchService;

    public DeepResearchService(ChatLanguageModel chatModel, TavlySearchService searchService) {
        this.chatModel = chatModel;
        this.searchService = searchService;
    }

    public ResearchResponse runResearch(String userQuery, ResearchMode mode, int maxIterations, Consumer<String> progressCallback) {
        if (progressCallback == null) progressCallback = log -> {}; // Default no-op callback

        return switch (mode) {
            case NORMAL -> runNormalAnswer(userQuery, progressCallback);
            case SMALL  -> runSmallResearch(userQuery, progressCallback);
            case DEEP   -> runDeepResearch(userQuery, maxIterations, progressCallback);
        };
    }

    private ResearchResponse runNormalAnswer(String userQuery, Consumer<String> progress) {
        progress.accept("Analyzing query directly without web search...");
        String prompt = String.format("""
            You are a concise technical assistant. Answer the user's question directly.
            
            STRICT CONSTRAINTS:
            - Word count: Maximum 100 words (2 to 3 concise sentences max).
            - Direct answer only. No tables, code blocks, or lists.
            
            Question: "%s"
            """, userQuery);

        String answer = chatModel.generate(prompt);
        return new ResearchResponse(userQuery, "Direct Knowledge Response", List.of("Mode: NORMAL"), 0, answer);
    }

    private ResearchResponse runSmallResearch(String userQuery, Consumer<String> progress) {
        int currentYear = Year.now().getValue();
        String searchQuery = userQuery + " " + currentYear;

        progress.accept("Executing Tavly web search query: " + searchQuery);
        List<SearchResult> searchResults = searchService.searchDetailed(searchQuery);

        progress.accept("Synthesizing 200-300 word summary from " + searchResults.size() + " web sources...");
        String rawText = searchResults.stream()
                .map(r -> "Title: " + r.title() + "\nURL: " + r.url() + "\nContent: " + r.content())
                .collect(Collectors.joining("\n\n"));

        String synthesisPrompt = String.format("""
            You are an efficient research assistant in %d. Synthesize a clean summary.
            
            STRICT CONSTRAINTS:
            - Target word count: 200 to 300 words.
            - Include 3 to 4 concise bullet points highlighting key metrics or takeaways.
            
            Question: "%s"
            
            SEARCH RESULTS:
            %s
            """, currentYear, userQuery, rawText);

        String answer = chatModel.generate(synthesisPrompt);
        String finalReportWithSources = answer + "\n\n" + formatSourcesMarkdown(searchResults);

        return new ResearchResponse(userQuery, "Single-Pass Search: " + searchQuery, List.of("Mode: SMALL"), 1, finalReportWithSources);
    }

    private ResearchResponse runDeepResearch(String userQuery, int maxIterations, Consumer<String> progress) {
        int currentYear = Year.now().getValue();

        // 1. Generate Research Strategy
        progress.accept("Formulating iterative research plan for: " + userQuery);
        String planPrompt = String.format("""
            You are an expert lead researcher in %d. Create a structured research strategy for: "%s"
            Provide exactly %d distinct web search queries. List each starting with a dash (-).
            """, currentYear, userQuery, maxIterations);

        String researchPlan = chatModel.generate(planPrompt);
        List<String> searchQueries = parsePlanQueries(researchPlan, userQuery, maxIterations);

        // 2. Iterative Search & Deduplication
        List<String> iterationFindings = new ArrayList<>();
        StringBuilder accumulatedContext = new StringBuilder();
        Map<String, String> uniqueSources = new LinkedHashMap<>();

        for (int i = 0; i < searchQueries.size(); i++) {
            String currentSearchQuery = searchQueries.get(i);
            progress.accept(String.format("Pass %d/%d: Searching web for '%s'...", (i + 1), searchQueries.size(), currentSearchQuery));

            List<SearchResult> results = searchService.searchDetailed(currentSearchQuery);
            for (SearchResult res : results) {
                uniqueSources.putIfAbsent(res.url(), res.title());
            }

            progress.accept(String.format("Pass %d/%d: Extracting insights from %d sources...", (i + 1), searchQueries.size(), results.size()));
            String searchContext = results.stream()
                    .map(r -> "Source: " + r.title() + " (" + r.url() + ")\n" + r.content())
                    .collect(Collectors.joining("\n---\n"));

            String extractionPrompt = String.format("""
                Extract facts, metrics, and insights from the web search results below.
                
                Goal: "%s"
                Sub-Query: "%s"
                
                SEARCH RESULTS:
                %s
                """, userQuery, currentSearchQuery, searchContext);

            String finding = chatModel.generate(extractionPrompt);
            iterationFindings.add("Iteration " + (i + 1) + " [" + currentSearchQuery + "]:\n" + finding);
            accumulatedContext.append("\n\n--- FINDINGS STEP ").append(i + 1).append(" ---\n").append(finding);
        }

        // 3. Final Report Synthesis
        progress.accept("Synthesizing 500+ word final deep research report...");
        String finalReportPrompt = String.format("""
            You are a Principal AI Architect writing a Deep Research Report in %d.

            Original Query: "%s"

            STRICT CONSTRAINTS:
            - Minimum word count: 500+ words.
            - Include Executive Summary, Comparative Markdown Table, Detailed Analysis, and Production Guidelines.
            - Ensure all sections finish cleanly without truncation.

            COLLECTED RESEARCH DATA:
            %s
            """, currentYear, userQuery, accumulatedContext.toString());

        String finalReport = chatModel.generate(finalReportPrompt);

        List<SearchResult> deduplicatedSources = uniqueSources.entrySet().stream()
                .map(entry -> new SearchResult(entry.getValue(), entry.getKey(), ""))
                .toList();

        String finalReportWithSources = finalReport + "\n\n" + formatSourcesMarkdown(deduplicatedSources);
        progress.accept("Deep research completed successfully!");

        return new ResearchResponse(userQuery, researchPlan, iterationFindings, iterationFindings.size(), finalReportWithSources);
    }

    private String formatSourcesMarkdown(List<SearchResult> sources) {
        if (sources.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("### Sources & References\n");
        int index = 1;
        for (SearchResult source : sources) {
            sb.append(String.format("%d. [%s](%s)\n", index++, source.title(), source.url()));
        }
        return sb.toString();
    }

    private List<String> parsePlanQueries(String planText, String fallbackQuery, int maxIterations) {
        List<String> queries = new ArrayList<>();
        if (planText == null || planText.isBlank()) return List.of(fallbackQuery);

        for (String line : planText.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.matches("^\\d+\\..*")) {
                String cleaned = trimmed.replaceFirst("^[-*\\d+\\.]\\s*", "").replaceAll("\"|'", "").trim();
                if (!cleaned.isBlank()) queries.add(cleaned);
            }
        }
        if (queries.isEmpty()) queries.add(fallbackQuery);
        return queries.subList(0, Math.min(queries.size(), maxIterations));
    }
}