package Shivu.ArbitraAI.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import Shivu.ArbitraAI.dto.SearchResult;
import java.util.ArrayList;
import java.util.List;

@Service
public class TavlySearchService {

    @Value("${app.tavly.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<SearchResult> searchDetailed(String query) {
        String url = "https://api.tavly.com/search";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
            "api_key", apiKey,
            "query", query,
            "search_depth", "basic",
            "max_results", 4
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        List<SearchResult> results = new ArrayList<>();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode resultsArray = root.path("results");

            if (resultsArray.isArray()) {
                for (JsonNode item : resultsArray) {
                    String title = item.path("title").asText("Source");
                    String resultUrl = item.path("url").asText("");
                    String content = item.path("content").asText("");

                    if (!resultUrl.isBlank()) {
                        results.add(new SearchResult(title, resultUrl, content));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Tavly search failed: " + e.getMessage());
        }

        return results;
    }
}