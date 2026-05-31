package com.architectai.design.architecture;

import java.util.List;

public record RoofPlan(
        String roofType,
        double slopeDegrees,
        double overhangMeters,
        List<RoofPlane> planes,
        List<String> drainageStrategy,
        List<String> climateFeatures
) {
    public record RoofPlane(
            String name,
            String direction,
            double slopeDegrees,
            String material
    ) {
    }
}
