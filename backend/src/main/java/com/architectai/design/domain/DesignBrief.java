package com.architectai.design.domain;

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
        List<String> constraints,
        String orientation,
        boolean parkingRequired,
        boolean lightwellRequired,
        boolean frontYardRequired,
        boolean rearGardenRequired,
        boolean openKitchen,
        String stairPreference,
        List<String> adjacencyPreferences,
        List<FloorRequirement> floorRequirements
) {
    public DesignBrief {
        rooms = rooms == null ? List.of() : rooms;
        preferences = preferences == null ? List.of() : preferences;
        constraints = constraints == null ? List.of() : constraints;
        orientation = orientation == null || orientation.isBlank() ? "unknown" : orientation;
        stairPreference = stairPreference == null || stairPreference.isBlank() ? "unknown" : stairPreference;
        adjacencyPreferences = adjacencyPreferences == null ? List.of() : adjacencyPreferences;
        floorRequirements = floorRequirements == null ? List.of() : floorRequirements;
    }

    public DesignBrief(
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
        this(
                siteWidthMeters,
                siteDepthMeters,
                floors,
                bedrooms,
                bathrooms,
                style,
                rooms,
                preferences,
                constraints,
                "unknown",
                false,
                false,
                false,
                false,
                false,
                "unknown",
                List.of(),
                List.of()
        );
    }

    public record FloorRequirement(
            int level,
            List<String> rooms
    ) {
        public FloorRequirement {
            rooms = rooms == null ? List.of() : rooms;
        }
    }
}
