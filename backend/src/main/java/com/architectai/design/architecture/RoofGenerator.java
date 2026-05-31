package com.architectai.design.architecture;

import com.architectai.design.domain.DesignBrief;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoofGenerator {

    public RoofPlan generate(DesignBrief brief, BuildingTypology typology, ClimateAnalysis climate, ExteriorStyle exteriorStyle) {
        String text = DesignText.combined(brief);
        String roofType;
        if (DesignText.containsAny(text, "green roof", "mai xanh", "mái xanh")) {
            roofType = "Green Roof";
        } else if (DesignText.containsAny(text, "thai", "mái thái", "mai thai")) {
            roofType = "Thai Roof";
        } else if (DesignText.containsAny(text, "japanese", "nhật", "nhat")) {
            roofType = "Japanese Roof";
        } else if (typology.code().startsWith("rural")) {
            roofType = "Gable Roof";
        } else if (typology.code().contains("villa") && DesignText.containsAny(exteriorStyle.name(), "luxury", "resort")) {
            roofType = "Hip Roof";
        } else if (brief.siteWidthMeters() < 5.5) {
            roofType = "Flat Roof";
        } else {
            roofType = "Mono Pitch Roof";
        }

        double slope = switch (roofType) {
            case "Thai Roof" -> 30;
            case "Gable Roof" -> 25;
            case "Hip Roof" -> 22;
            case "Japanese Roof" -> 18;
            case "Mono Pitch Roof" -> 8;
            case "Green Roof" -> 3;
            default -> 2;
        };
        double overhang = brief.siteWidthMeters() >= 6 ? 0.9 : 0.45;

        return new RoofPlan(
                roofType,
                slope,
                overhang,
                List.of(
                        new RoofPlan.RoofPlane("main roof plane", drainageDirection(brief.orientation()), slope, roofMaterial(roofType)),
                        new RoofPlan.RoofPlane("courtyard or stair skylight cap", "internal courtyard drain", Math.min(8, slope), "laminated glass with operable vent")
                ),
                List.of("ẩn máng xối về trục kỹ thuật", "thu nước mưa cho tưới cây sân trong", "tránh xả trực tiếp sang nhà lân cận"),
                climate.shadingStrategy().stream().limit(2).toList()
        );
    }

    private String drainageDirection(String orientation) {
        if (orientation == null || "unknown".equals(orientation)) {
            return "rear and internal rainwater pipe";
        }
        return "away from " + orientation + " facade toward rear technical shaft";
    }

    private String roofMaterial(String roofType) {
        return switch (roofType) {
            case "Green Roof" -> "lightweight planted roof build-up";
            case "Thai Roof", "Gable Roof", "Hip Roof", "Japanese Roof" -> "ventilated clay or concrete tile";
            default -> "insulated reinforced concrete slab with waterproofing";
        };
    }
}
