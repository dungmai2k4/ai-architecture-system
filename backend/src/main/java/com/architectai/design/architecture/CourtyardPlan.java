package com.architectai.design.architecture;

import java.util.List;

public record CourtyardPlan(
        String type,
        double x,
        double y,
        double width,
        double depth,
        boolean waterFeature,
        List<String> treePlacement,
        List<String> skylightPlacement,
        List<String> designNotes
) {
}
