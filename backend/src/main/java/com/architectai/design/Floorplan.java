package com.architectai.design;

import java.util.List;

public record Floorplan(
        double siteWidth,
        double siteDepth,
        List<FloorplanRoom> rooms,
        List<FloorplanWall> walls,
        List<FloorplanDoor> doors,
        List<FloorplanWindow> windows,
        List<FloorplanFurniture> furniture,
        String svg
) {
}
