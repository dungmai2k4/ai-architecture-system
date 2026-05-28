package com.architectai.design;

public record FloorplanFurniture(
        String label,
        String type,
        double x,
        double y,
        double width,
        double depth,
        double rotation,
        String color
) {
}
