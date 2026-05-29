package com.architectai.design;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class RenderPromptGenerator {

    private final VietnameseRegionalDesignGuide regionalDesignGuide;

    public RenderPromptGenerator(VietnameseRegionalDesignGuide regionalDesignGuide) {
        this.regionalDesignGuide = regionalDesignGuide;
    }

    public String generate(DesignBrief brief, LayoutPlan layoutPlan, Floorplan floorplan) {
        VietnameseRegionalDesignGuide.RegionalDesignProfile profile = regionalDesignGuide.resolve(brief);
        String rooms = describeRooms(floorplan.floors());
        String zoning = formatSentenceList(layoutPlan.zoning());
        String notes = formatSentenceList(layoutPlan.notes());
        String preferences = formatList(brief.preferences());
        String constraints = formatList(brief.constraints());
        String layoutIntent = describeLayoutIntent(brief);

        return "Architectural visualization prompt: Vietnamese townhouse on a "
                + formatMeters(brief.siteWidthMeters())
                + "m x "
                + formatMeters(brief.siteDepthMeters())
                + "m urban lot, "
                + brief.floors()
                + " floor(s), "
                + brief.bedrooms()
                + " bedroom(s), "
                + brief.bathrooms()
                + " bathroom(s), "
                + normalizeStyle(brief.style())
                + " style, inspired by "
                + profile.typology()
                + ". Regional material palette: "
                + profile.materialPalette()
                + ". Layout strategy: "
                + layoutPlan.strategy()
                + ". Zoning: "
                + zoning
                + ". Key spaces: "
                + rooms
                + ". Design notes: "
                + notes
                + ". Layout intent: "
                + layoutIntent
                + ". User preferences: "
                + preferences
                + ". Constraints: "
                + constraints
                + ". Render a clean front perspective and a bright interior mood board; realistic materials, natural daylight, compact Vietnamese urban housing proportions, climate-responsive facade with privacy screens and greenery, no text labels, no dimensions, no people.";
    }

    private String describeLayoutIntent(DesignBrief brief) {
        String parking = brief.parkingRequired() ? "parking required" : "parking not specified";
        String lightwell = brief.lightwellRequired() ? "lightwell or courtyard required" : "lightwell not specified";
        String frontYard = brief.frontYardRequired() ? "front yard or porch required" : "front yard not specified";
        String rearGarden = brief.rearGardenRequired() ? "rear garden or service yard required" : "rear garden not specified";
        String openKitchen = brief.openKitchen() ? "open kitchen requested" : "open kitchen not specified";
        String adjacency = formatList(brief.adjacencyPreferences(), "; ");
        String floorRequirements = brief.floorRequirements().isEmpty()
                ? "none"
                : brief.floorRequirements().stream()
                .map(requirement -> "floor " + requirement.level() + ": " + formatList(requirement.rooms()))
                .collect(Collectors.joining("; "));
        return "orientation " + brief.orientation()
                + ", stair preference " + brief.stairPreference()
                + ", " + parking
                + ", " + lightwell
                + ", " + frontYard
                + ", " + rearGarden
                + ", " + openKitchen
                + ", adjacency wishes: " + adjacency
                + ", floor requirements: " + floorRequirements;
    }

    private String describeRooms(List<FloorplanLevel> floors) {
        if (floors == null || floors.isEmpty()) {
            return "living room, kitchen, bedrooms, bathrooms";
        }

        return floors.stream()
                .map(floor -> floor.label() + " includes " + formatRoomLabels(floor.rooms()))
                .collect(Collectors.joining("; "));
    }

    private String normalizeStyle(String style) {
        if (style == null || style.isBlank()) {
            return "modern Vietnamese";
        }
        return style.trim().toLowerCase(Locale.ROOT);
    }

    private String formatRoomLabels(List<FloorplanRoom> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return "main functional spaces";
        }

        String labels = rooms.stream()
                .filter(room -> !"partition".equals(room.type()))
                .map(FloorplanRoom::label)
                .filter(label -> label != null && !label.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));
        return labels.isBlank() ? "main functional spaces" : labels;
    }

    private String formatSentenceList(List<String> values) {
        return formatList(values, "; ");
    }

    private String formatList(List<String> values) {
        return formatList(values, ", ");
    }

    private String formatList(List<String> values, String delimiter) {
        if (values == null || values.isEmpty()) {
            return "none";
        }

        String joined = values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(delimiter));
        return joined.isBlank() ? "none" : joined;
    }

    private String formatMeters(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((int) value);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
