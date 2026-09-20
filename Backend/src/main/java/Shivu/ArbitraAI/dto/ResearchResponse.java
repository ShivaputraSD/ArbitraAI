package Shivu.ArbitraAI.dto;
import java.util.List;

public record ResearchResponse(
    String userQuery,
    String researchPlan,
    List<String> iterationFindings,
    int totalIterations,
    String finalReport
) {}