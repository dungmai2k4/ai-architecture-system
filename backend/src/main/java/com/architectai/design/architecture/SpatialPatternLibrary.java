package com.architectai.design.architecture;

import com.architectai.design.domain.DesignBrief;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SpatialPatternLibrary {

    private final ArchitecturalKnowledgeBase knowledgeBase;

    public SpatialPatternLibrary(ArchitecturalKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public List<SpatialPattern> selectPatterns(DesignBrief brief, BuildingTypology typology) {
        JsonNode patterns = knowledgeBase.dataset("spatial-patterns");
        List<SpatialPattern> selected = new ArrayList<>();
        for (JsonNode pattern : patterns) {
            if (isRecommended(pattern, typology) || isRequested(brief, pattern.path("code").asText())) {
                selected.add(toPattern(pattern));
            }
            if (selected.size() == 2) {
                break;
            }
        }
        if (selected.isEmpty() && patterns.isArray() && !patterns.isEmpty()) {
            selected.add(toPattern(patterns.get(0)));
        }
        return selected;
    }

    private boolean isRecommended(JsonNode pattern, BuildingTypology typology) {
        String code = pattern.path("code").asText();
        return typology.recommendedPatterns().stream().anyMatch(code::equalsIgnoreCase);
    }

    private boolean isRequested(DesignBrief brief, String code) {
        String text = DesignText.combined(brief);
        return switch (code) {
            case "central-courtyard" -> brief.lightwellRequired() || DesignText.containsAny(text, "central courtyard", "sân trong giữa", "giếng trời giữa");
            case "front-courtyard" -> brief.frontYardRequired() || DesignText.containsAny(text, "sân trước", "front courtyard");
            case "side-courtyard" -> DesignText.containsAny(text, "sân bên", "side courtyard", "thang lệch bên");
            case "l-shaped-layout" -> DesignText.containsAny(text, "chữ l", "l-shaped");
            case "u-shaped-layout" -> DesignText.containsAny(text, "chữ u", "u-shaped");
            case "multi-generation-cluster" -> DesignText.containsAny(text, "nhiều thế hệ", "ông bà", "elderly");
            default -> false;
        };
    }

    private SpatialPattern toPattern(JsonNode pattern) {
        return new SpatialPattern(
                pattern.path("code").asText(),
                pattern.path("name").asText(),
                pattern.path("intent").asText(),
                toList(pattern.path("zones")),
                toList(pattern.path("circulationRules")),
                toList(pattern.path("suitableTypologies"))
        );
    }

    private List<String> toList(JsonNode values) {
        List<String> result = new ArrayList<>();
        if (values.isArray()) {
            values.forEach(value -> result.add(value.asText()));
        }
        return result;
    }
}
