package com.architectai.design;

import java.util.List;

public record Floorplan(
        double siteWidth,
        double siteDepth,
        List<FloorplanRoom> rooms,
        String svg
) {
}
