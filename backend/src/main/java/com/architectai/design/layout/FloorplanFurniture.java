package com.architectai.design.layout;

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
