package Shivu.ArbitraAI.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "research_reports")
public class ResearchReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String query;

    private String mode;

    @Column(columnDefinition = "TEXT")
    private String finalReport;

    private LocalDateTime createdAt;

    public ResearchReport() {}

    public ResearchReport(String query, String mode, String finalReport) {
        this.query = query;
        this.mode = mode;
        this.finalReport = finalReport;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getFinalReport() { return finalReport; }
    public void setFinalReport(String finalReport) { this.finalReport = finalReport; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}