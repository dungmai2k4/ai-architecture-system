package com.architectai.design.architecture;

import com.architectai.design.domain.DesignBrief;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourtyardPlanner {

    public CourtyardPlan plan(DesignBrief brief, BuildingTypology typology, List<SpatialPattern> patterns, ClimateAnalysis climate) {
        String patternCode = patterns.isEmpty() ? "side-courtyard" : patterns.get(0).code();
        String type = switch (patternCode) {
            case "front-courtyard" -> "front";
            case "l-shaped-layout", "u-shaped-layout" -> "garden court";
            case "side-courtyard" -> "side";
            default -> "central";
        };

        double width = switch (type) {
            case "front" -> brief.siteWidthMeters();
            case "side" -> round(Math.max(1.1, Math.min(1.8, brief.siteWidthMeters() * 0.28)));
            case "garden court" -> round(Math.max(2.8, brief.siteWidthMeters() * 0.38));
            default -> round(Math.max(1.6, Math.min(2.8, brief.siteWidthMeters() * 0.42)));
        };
        double depth = switch (type) {
            case "front" -> round(Math.max(1.8, Math.min(3.2, brief.siteDepthMeters() * 0.13)));
            case "side" -> round(Math.max(3.2, Math.min(5.5, brief.siteDepthMeters() * 0.24)));
            case "garden court" -> round(Math.max(3.0, Math.min(6.0, brief.siteDepthMeters() * 0.24)));
            default -> round(Math.max(2.2, Math.min(4.2, brief.siteDepthMeters() * 0.18)));
        };
        double x = switch (type) {
            case "front", "central", "garden court" -> round((brief.siteWidthMeters() - width) / 2);
            default -> 0;
        };
        double y = switch (type) {
            case "front" -> 0;
            case "garden court" -> round(brief.siteDepthMeters() * 0.28);
            case "side" -> round(brief.siteDepthMeters() * 0.34);
            default -> round(brief.siteDepthMeters() * 0.38);
        };
        boolean waterFeature = brief.siteWidthMeters() >= 5 || DesignText.containsAny(climate.climateZone(), "southern", "central");

        return new CourtyardPlan(
                type,
                x,
                y,
                width,
                depth,
                waterFeature,
                List.of("cây tán nhỏ tại góc không cản luồng gió", "bồn cây thấp dọc mép mở về phòng khách/bếp"),
                List.of("skylight kính mờ hoặc polycarbonate có lam che mưa", "ô mở đứng thông tầng cạnh thang để hút khí nóng"),
                List.of("Tỷ lệ sân trong ưu tiên vi khí hậu thay vì chỉ là khoảng trống kỹ thuật.", "Kết nối visual giữa phòng khách, thang và bếp để tăng chiều sâu không gian.")
        );
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
