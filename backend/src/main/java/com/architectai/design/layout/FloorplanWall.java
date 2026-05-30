package com.architectai.design.layout;

public record FloorplanWall(
        double x1,
        double y1,
        double x2,
        double y2,
        double thickness,
        String type
) {
}
