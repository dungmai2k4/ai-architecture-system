package com.architectai.design.architecture;

import java.util.List;

public record ExteriorStyle(
        String name,
        List<String> materialPalette,
        List<String> facadeRules,
        List<String> balconyRules,
        List<String> landscapeRules
) {
}
