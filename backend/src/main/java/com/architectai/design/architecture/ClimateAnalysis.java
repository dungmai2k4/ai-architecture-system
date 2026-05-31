package com.architectai.design.architecture;

import java.util.List;

public record ClimateAnalysis(
        String location,
        String orientation,
        String climateZone,
        List<String> shadingStrategy,
        List<String> ventilationStrategy,
        List<String> daylightStrategy,
        List<String> greenBufferStrategy
) {
}
