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
        String location,
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
        if (looksLikeReversedTownhouseDimensions(siteWidthMeters, siteDepthMeters)) {
            double frontage = siteDepthMeters;
            siteDepthMeters = siteWidthMeters;
            siteWidthMeters = frontage;
        }
        rooms = rooms == null ? List.of() : rooms;
        preferences = preferences == null ? List.of() : preferences;
        constraints = constraints == null ? List.of() : constraints;
        orientation = orientation == null || orientation.isBlank() ? "unknown" : orientation;
        location = location == null || location.isBlank() ? "unknown" : location;
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

    public DesignBrief(
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
                orientation,
                "unknown",
                parkingRequired,
                lightwellRequired,
                frontYardRequired,
                rearGardenRequired,
                openKitchen,
                stairPreference,
                adjacencyPreferences,
                floorRequirements
        );
    }


    private static boolean looksLikeReversedTownhouseDimensions(double siteWidthMeters, double siteDepthMeters) {
        return siteWidthMeters >= 12
                && siteDepthMeters > 0
                && siteDepthMeters <= 8
                && siteWidthMeters / siteDepthMeters >= 1.6;
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
