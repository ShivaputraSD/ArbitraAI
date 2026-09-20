package Shivu.ArbitraAI.service;

import Shivu.ArbitraAI.dto.ResearchResponse;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeepResearchService {

    private final ChatLanguageModel chatModel;
    private final TavlySearchService searchService;

    public DeepResearchService(ChatLanguageModel chatModel, TavlySearchService searchService) {
        this.chatModel = chatModel;
        this.searchService = searchService;
    }

    public ResearchResponse runResearch(String userQuery, int maxIterations) {
        int currentYear = Year.now().getValue();

        System.out.println("==================================================");
        System.out.println(">>> Starting Deep Research Loop for: " + userQuery);
        System.out.println(">>> Current Year Context: " + currentYear);
        System.out.println("==================================================");

        // Step 1: Generate Research Strategy & Search Queries anchored to current year
        String planPrompt = String.format("""
            You are an expert lead researcher in %d. Create a structured research strategy for the query:
            "%s"

            Provide exactly %d distinct, highly targeted web search queries focusing on current developments up to %d.
            Ensure queries use terms like "%d developments", "latest benchmarks", or "production best practices".
            List each search query on a new line as a clean bullet point starting with a dash (-).
            """, currentYear, userQuery, maxIterations, currentYear, currentYear);

        String researchPlan = chatModel.generate(planPrompt);
        System.out.println(">>> RESEARCH PLAN GENERATED:\n" + researchPlan);

        List<String> searchQueries = parsePlanQueries(researchPlan, userQuery, maxIterations);

        // Step 2: Iterative Search & Synthesis Loop
        List<String> iterationFindings = new ArrayList<>();
        StringBuilder accumulatedContext = new StringBuilder();

        for (int i = 0; i < searchQueries.size(); i++) {
            String currentSearchQuery = searchQueries.get(i);
            int iterationNum = i + 1;

            System.out.println("\n--------------------------------------------------");
            System.out.println(">>> Iteration " + iterationNum + "/" + searchQueries.size() + " - Search Query: " + currentSearchQuery);
            System.out.println("--------------------------------------------------");

            // Fetch live web results
            String rawSearchResults = searchService.search(currentSearchQuery);

            // Terminal Debug Print: Check what Tavly returned
            System.out.println(">>> RAW SEARCH RESULTS SNIPPET:");
            if (rawSearchResults != null && !rawSearchResults.isBlank()) {
                int previewLength = Math.min(300, rawSearchResults.length());
                System.out.println(rawSearchResults.substring(0, previewLength) + "...\n");
            } else {
                System.out.println("[WARNING] Search results came back empty or null!\n");
                rawSearchResults = "No web search results could be retrieved for query: " + currentSearchQuery;
            }

            // Analyze search context with LLM
            String extractionPrompt = String.format("""
                You are a senior research analyst. Extract concrete facts, metrics, framework names, benchmarks, and technical insights from the web search results provided below.

                Original Goal: "%s"
                Sub-Query Target: "%s"

                RAW WEB SEARCH RESULTS:
                %s

                Instructions:
                1. Pull out specific names, versions, metrics, benchmarks, pros/cons, and real-world examples present in the text.
                2. Do NOT output empty dashes (-) or generic placeholders. If the search results contain information, synthesize it thoroughly into detailed bullet points.
                3. If the search results are missing data, explicitly state what search terms failed and extract whatever relevant context exists.
                """, userQuery, currentSearchQuery, rawSearchResults);

            String finding = chatModel.generate(extractionPrompt);

            System.out.println(">>> SYNTHESIZED FINDINGS FOR ITERATION " + iterationNum + ":\n" + finding);

            String structuredFinding = "Iteration " + iterationNum + " [" + currentSearchQuery + "]:\n" + finding;
            iterationFindings.add(structuredFinding);

            accumulatedContext.append("\n\n--- RESEARCH FINDINGS STEP ").append(iterationNum)
                              .append(" (Query: ").append(currentSearchQuery).append(") ---\n")
                              .append(finding);
        }

        // Step 3: Final Report Synthesis
        System.out.println("\n==================================================");
        System.out.println(">>> Generating Final Deep Research Synthesis Report...");
        System.out.println("==================================================");

        String finalReportPrompt = String.format("""
            You are a Principal AI Architect writing a comprehensive Deep Research Report in %d.

            Original Query: "%s"

            Synthesize a cohesive, publication-ready research report based on the collected research findings below:

            COLLECTED RESEARCH DATA:
            %s

            Report Structure Requirements:
            # Deep Research Report: %s (%d Edition)

            ## 1. Executive Summary
            - Key takeaways, current state, and strategic outlook.
            - Include a summary Markdown Table highlighting core facets or metrics.

            ## 2. Comprehensive Findings & Technical Breakdown
            - Detailed examination of frameworks, tools, methodologies, or benchmarks discovered.
            - Use clear bullet points, code concepts, or architectural notes where applicable.

            ## 3. Comparative Analysis & Trade-offs
            - Strengths vs. Weaknesses, production readiness, scalability, or cost factors.

            ## 4. Key Recommendations & Future Outlook
            - Actionable next steps and guidance for technical decision-makers in %d.
            """, currentYear, userQuery, accumulatedContext.toString(), userQuery, currentYear, currentYear);

        String finalReport = chatModel.generate(finalReportPrompt);

        return new ResearchResponse(
            userQuery,
            researchPlan,
            iterationFindings,
            iterationFindings.size(),
            finalReport
        );
    }

    private List<String> parsePlanQueries(String planText, String fallbackQuery, int maxIterations) {
        List<String> queries = new ArrayList<>();
        if (planText == null || planText.isBlank()) {
            queries.add(fallbackQuery);
            return queries;
        }

        String[] lines = planText.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.matches("^\\d+\\..*")) {
                String cleaned = trimmed.replaceFirst("^[-*\\d+\\.]\\s*", "")
                                        .replaceAll("\"|'", "")
                                        .trim();
                if (!cleaned.isBlank()) {
                    queries.add(cleaned);
                }
            }
        }

        if (queries.isEmpty()) {
            queries.add(fallbackQuery);
        }

        return queries.subList(0, Math.min(queries.size(), maxIterations));
    }
}