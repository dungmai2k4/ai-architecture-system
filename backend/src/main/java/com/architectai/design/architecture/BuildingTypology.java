package com.architectai.design.architecture;

import java.util.List;

public record BuildingTypology(
        String code,
        String name,
        String description,
        List<String> fitReasons,
        List<String> planningPriorities,
        List<String> recommendedPatterns
) {
}
