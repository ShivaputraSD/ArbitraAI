package Shivu.ArbitraAI.controller;

import Shivu.ArbitraAI.dto.ResearchMode;
import Shivu.ArbitraAI.dto.ResearchRequest;
import Shivu.ArbitraAI.dto.ResearchResponse;
import Shivu.ArbitraAI.entity.ResearchReport;
import Shivu.ArbitraAI.repository.ResearchReportRepository;
import Shivu.ArbitraAI.service.DeepResearchService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ResearchController {

    private final DeepResearchService researchService;
    private final ResearchReportRepository reportRepository;

    public ResearchController(DeepResearchService researchService, ResearchReportRepository reportRepository) {
        this.researchService = researchService;
        this.reportRepository = reportRepository;
    }

    @PostMapping("/research")
    public ResponseEntity<?> performResearch(@RequestBody ResearchRequest request) {
        if (request.query() == null || request.query().isBlank()) {
            return ResponseEntity.badRequest().body("Error: Field 'query' cannot be empty.");
        }

        try {
            ResearchResponse response = researchService.runResearch(
                request.query(),
                request.getModeOrDefault(),
                request.getMaxIterationsOrDefault(),
                null
            );

            ResearchReport savedReport = new ResearchReport(
                request.query(),
                request.getModeOrDefault().name(),
                response.finalReport()
            );
            reportRepository.save(savedReport);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Research Execution Error: " + e.getMessage());
        }
    }

    @GetMapping(value = "/research/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamResearch(@RequestParam String query, 
                                     @RequestParam(defaultValue = "DEEP") ResearchMode mode,
                                     @RequestParam(defaultValue = "2") int maxIterations) {
        
        SseEmitter emitter = new SseEmitter(300000L); // 5-minute timeout for deep searches

        CompletableFuture.runAsync(() -> {
            try {
                ResearchResponse response = researchService.runResearch(
                    query,
                    mode,
                    maxIterations,
                    progressMsg -> {
                        try {
                            emitter.send(SseEmitter.event().name("status").data(progressMsg));
                        } catch (Exception e) {
                            // Client disconnected
                        }
                    }
                );

                // Save to database
                ResearchReport savedReport = new ResearchReport(query, mode.name(), response.finalReport());
                reportRepository.save(savedReport);

                // Send final report event
                emitter.send(SseEmitter.event().name("complete").data(response.finalReport()));
                emitter.complete();

            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data("Execution error: " + e.getMessage()));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @GetMapping("/history")
    public ResponseEntity<List<ResearchReport>> getHistory() {
        return ResponseEntity.ok(reportRepository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<?> getHistoryById(@PathVariable Long id) {
        return reportRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}