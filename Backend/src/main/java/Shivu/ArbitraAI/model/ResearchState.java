package Shivu.ArbitraAI.model;
import java.util.ArrayList;
import java.util.List;

public record ResearchState(
        String userQuery,
        String researchQuestion,
        List<String> researchPlan,
        List<String> searchQueries,
        List<String> searchResults,
        List<String> findings,
        boolean needsMoreResearch,
        int researchIterations,
        String finalAnswer
) {
    public static ResearchState initial(String query) {
        return new ResearchState(
                query, "", new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), true, 0, ""
        );
    }
}