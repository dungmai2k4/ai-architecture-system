package com.architectai.design.architecture;

import com.architectai.design.domain.DesignBrief;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClimateEngine {

    private final ArchitecturalKnowledgeBase knowledgeBase;

    public ClimateEngine(ArchitecturalKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public ClimateAnalysis analyze(DesignBrief brief) {
        String location = brief.location();
        String orientation = brief.orientation();
        String climateZone = resolveClimateZone(location, brief);
        JsonNode tropicalRules = knowledgeBase.dataset("tropical-design-rules");

        List<String> shading = new ArrayList<>(toList(tropicalRules.path("shading")));
        List<String> ventilation = new ArrayList<>(toList(tropicalRules.path("ventilation")));
        List<String> daylight = new ArrayList<>(toList(tropicalRules.path("daylight")));
        List<String> green = new ArrayList<>(toList(tropicalRules.path("greenBuffers")));

        if ("west".equals(orientation) || "southwest".equals(orientation) || "northwest".equals(orientation)) {
            shading.add("tăng lam đứng hai lớp và ban công đệm cho mặt tiền " + orientation);
            daylight.add("hạn chế kính lớn trực diện hướng " + orientation + ", ưu tiên lấy sáng gián tiếp từ sân trong");
        }
        if (brief.siteDepthMeters() >= 20 || brief.lightwellRequired()) {
            ventilation.add("dùng giếng trời lệch tâm để kích hoạt thông gió ống khói cho lô sâu");
            daylight.add("bổ sung skylight chống mưa trên lõi thang/sân trong");
        }
        if (DesignText.containsAny(DesignText.combined(brief), "coastal", "ven bien", "biển", "da nang", "nha trang")) {
            shading.add("chọn vật liệu và lam chịu hơi muối, tránh chi tiết kim loại khó bảo trì");
        }

        return new ClimateAnalysis(location, orientation, climateZone, shading, ventilation, daylight, green);
    }

    private String resolveClimateZone(String location, DesignBrief brief) {
        String text = DesignText.normalize(location + " " + DesignText.combined(brief));
        if (DesignText.containsAny(text, "ha noi", "mien bac", "northern", "bac bo")) {
            return "northern humid subtropical Vietnam";
        }
        if (DesignText.containsAny(text, "hue", "da nang", "mien trung", "central", "coastal")) {
            return "central coastal hot humid Vietnam";
        }
        if (DesignText.containsAny(text, "sai gon", "ho chi minh", "mien nam", "mekong", "can tho")) {
            return "southern tropical monsoon Vietnam";
        }
        return "Vietnam tropical monsoon baseline";
    }

    private List<String> toList(JsonNode values) {
        List<String> result = new ArrayList<>();
        if (values.isArray()) {
            values.forEach(value -> result.add(value.asText()));
        }
        return result;
    }
}
