package com.architectai.design;

public record FloorplanDoor(
        String label,
        double x,
        double y,
        double width,
        String orientation,
        String swing
) {
}
