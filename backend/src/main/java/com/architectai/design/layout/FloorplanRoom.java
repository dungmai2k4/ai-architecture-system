package com.architectai.design.layout;

public record FloorplanRoom(
        String name,
        String label,
        String type,
        double x,
        double y,
        double width,
        double depth,
        String color
) {
}
