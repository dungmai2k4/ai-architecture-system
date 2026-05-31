package com.architectai.design.architecture;

import java.util.List;

public record FacadeComposition(
        String compositionType,
        int bays,
        List<String> balconies,
        List<String> verticalFins,
        List<String> sunScreens,
        List<String> greenFacade,
        List<String> roofOverhangs,
        List<String> facadeRules
) {
}
