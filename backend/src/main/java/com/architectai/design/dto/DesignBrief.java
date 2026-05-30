package com.architectai.design;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DesignBrief(
        double siteWidthMeters,
        double siteDepthMeters,
        int floors,
        int bedrooms,
        int bathrooms,
        String style,
        List<String> rooms,
        List<String> preferences,
        List<String> constraints
) {
}
