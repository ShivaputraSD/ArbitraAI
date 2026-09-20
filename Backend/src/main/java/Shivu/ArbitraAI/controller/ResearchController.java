package Shivu.ArbitraAI.controller;

import Shivu.ArbitraAI.dto.ResearchRequest;
import Shivu.ArbitraAI.dto.ResearchResponse;
import Shivu.ArbitraAI.service.DeepResearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ResearchController {

    private final DeepResearchService researchService;

    public ResearchController(DeepResearchService researchService) {
        this.researchService = researchService;
    }

    @PostMapping("/research")
    public ResponseEntity<?> performResearch(@RequestBody ResearchRequest request) {
        if (request.query() == null || request.query().isBlank()) {
            return ResponseEntity.badRequest().body("Error: Field 'query' cannot be empty.");
        }

        try {
            ResearchResponse response = researchService.runResearch(
                request.query(),
                request.getMaxIterationsOrDefault()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Deep Research Error: " + e.getMessage());
        }
    }
}