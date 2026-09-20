package Shivu.ArbitraAI.dto;

public record ResearchRequest(
    String query,
    Integer maxIterations
) {
    public int getMaxIterationsOrDefault() {
        return (maxIterations != null && maxIterations > 0) ? maxIterations : 2;
    }
}