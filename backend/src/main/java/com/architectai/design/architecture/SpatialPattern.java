package com.architectai.design.architecture;

import java.util.List;

public record SpatialPattern(
        String code,
        String name,
        String intent,
        List<String> zones,
        List<String> circulationRules,
        List<String> suitableTypologies
) {
}
