package Shivu.ArbitraAI.dto;

public record ResearchRequest(
    String query,
    ResearchMode mode,
    Integer maxIterations
) {
    public ResearchMode getModeOrDefault() {
        return mode != null ? mode : ResearchMode.DEEP;
    }

    public int getMaxIterationsOrDefault() {
        return (maxIterations != null && maxIterations > 0) ? maxIterations : 2;
    }
}