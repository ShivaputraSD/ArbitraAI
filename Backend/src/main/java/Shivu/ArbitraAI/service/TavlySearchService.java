package Shivu.ArbitraAI.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class TavlySearchService {

    @Value("${app.tavly.api-key}")
    private String tavlyApiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String search(String query) {
        try {
            Map<String, Object> payload = Map.of(
                "api_key", tavlyApiKey,
                "query", query,
                "search_depth", "basic",
                "max_results", 5
            );

            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.tavly.com/search"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Tavly API Error (" + response.statusCode() + "): " + response.body());
                return "Search failed with status code: " + response.statusCode();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode results = root.path("results");

            if (!results.isArray() || results.isEmpty()) {
                return "No web results retrieved for query: " + query;
            }

            StringBuilder formattedResults = new StringBuilder();
            int index = 1;
            for (JsonNode item : results) {
                String title = item.path("title").asText("");
                String url = item.path("url").asText("");
                String content = item.path("content").asText("");

                formattedResults.append("[").append(index++).append("] ")
                               .append(title).append("\n")
                               .append("Source: ").append(url).append("\n")
                               .append("Snippet: ").append(content).append("\n\n");
            }

            return formattedResults.toString();

        } catch (Exception e) {
            System.err.println("Exception during Tavly API call: " + e.getMessage());
            return "Search execution error: " + e.getMessage();
        }
    }
}