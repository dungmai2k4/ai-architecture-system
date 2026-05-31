package com.architectai.design.architecture;

import com.architectai.design.domain.DesignBrief;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExteriorStyleEngine {

    public ExteriorStyle generate(DesignBrief brief, BuildingTypology typology, ClimateAnalysis climate) {
        String text = DesignText.combined(brief);
        String name;
        if (DesignText.containsAny(text, "luxury", "sang trọng", "sang trong")) {
            name = "Luxury Villa";
        } else if (DesignText.containsAny(text, "minimal", "toi gian", "tối giản")) {
            name = "Minimalist";
        } else if (DesignText.containsAny(text, "resort", "nghi duong", "nghỉ dưỡng") || "riverside-house".equals(typology.code())) {
            name = "Tropical Resort";
        } else if (DesignText.containsAny(text, "vietnamese", "viet nam", "việt nam", "gach", "gạch", "go", "gỗ")) {
            name = "Contemporary Vietnamese";
        } else {
            name = "Modern Tropical";
        }

        return switch (name) {
            case "Luxury Villa" -> new ExteriorStyle(
                    name,
                    List.of("travertine or light stone", "bronze aluminum fins", "warm timber soffit", "low iron glass"),
                    List.of("compose facade with deep portals", "recess large glazing behind shaded frame", "use solid plinth for privacy"),
                    List.of("wide balcony as outdoor room", "glass or bronze railing recessed behind planter"),
                    List.of("sculptural tree at arrival", "layered shrubs and water reflection", "concealed landscape lighting")
            );
            case "Minimalist" -> new ExteriorStyle(
                    name,
                    List.of("white mineral render", "pale concrete", "black metal", "clear glass"),
                    List.of("simple volumes", "recessed openings", "few strong horizontal lines"),
                    List.of("thin slab balcony", "integrated planter only where needed for shading"),
                    List.of("low maintenance greenery", "single specimen tree", "gravel or terrazzo court")
            );
            case "Contemporary Vietnamese" -> new ExteriorStyle(
                    name,
                    List.of("gạch thông gió", "gỗ ấm", "terrazzo", "mái/ngói hiện đại"),
                    List.of("layer brick screens for privacy", "use porch depth as climate buffer", "balance solid wall and filtered openings"),
                    List.of("balcony with planter and perforated screen", "timber handrail or warm metal detail"),
                    List.of("sân trong có cây bản địa", "bồn cây ban công", "mặt nước nhỏ làm mát")
            );
            case "Tropical Resort" -> new ExteriorStyle(
                    name,
                    List.of("natural stone", "dark timber", "bamboo or timber-look louvers", "textured plaster"),
                    List.of("large veranda language", "deep eaves", "open corners facing garden or water"),
                    List.of("balcony as shaded terrace", "planters with cascading tropical plants"),
                    List.of("dense layered planting", "water feature", "stepping stone and shaded seating")
            );
            default -> new ExteriorStyle(
                    name,
                    List.of("light render", "wood-look aluminum", "terracotta screen", "green planters", "clear and frosted glass"),
                    List.of("shade the facade before adding glass", "alternate solid privacy panels with breathable screens", "express stair/lightwell as vertical green slot"),
                    List.of("balcony depth at least a usable shading ledge", "integrate planters and drainage", "screen west-facing balconies"),
                    climate.greenBufferStrategy()
            );
        };
    }
}
