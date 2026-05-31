package com.architectai.design.architecture;

import java.util.List;

public record ArchitecturalDrawings(
        DrawingSheet exteriorPerspective,
        DrawingSheet roofPlan,
        DrawingSheet longitudinalSection,
        DrawingSheet frontElevation,
        List<String> drawingNotes
) {
    public record DrawingSheet(
            String title,
            String drawingType,
            String scale,
            String svg,
            List<String> callouts
    ) {
    }
}
