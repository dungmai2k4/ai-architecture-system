package com.architectai.design.architecture;

import java.util.List;

public record BuildingSection(
        String name,
        List<SectionLevel> levels,
        String stairSection,
        String roofSection,
        String skylightSection,
        List<String> notes
) {
    public record SectionLevel(
            int level,
            double floorHeightMeters,
            double slabThicknessMeters,
            String primaryUse
    ) {
    }
}
