package com.architectai.design.layout;

import java.util.List;

public record FloorplanLevel(
        int level,
        String label,
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
