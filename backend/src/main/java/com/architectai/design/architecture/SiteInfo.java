package com.architectai.design.architecture;

public record SiteInfo(
        double widthMeters,
        double depthMeters,
        double areaSquareMeters,
        String location,
        String orientation,
        String urbanContext
) {
}
