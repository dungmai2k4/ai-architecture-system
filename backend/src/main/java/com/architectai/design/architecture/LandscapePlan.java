package com.architectai.design.architecture;

import java.util.List;

public record LandscapePlan(
        List<String> frontLandscape,
        List<String> courtyardLandscape,
        List<String> rearLandscape,
        List<String> balconyPlanting,
        List<String> waterAndShadeElements
) {
}
