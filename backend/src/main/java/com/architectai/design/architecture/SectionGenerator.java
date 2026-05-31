package com.architectai.design.architecture;

import com.architectai.design.domain.DesignBrief;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SectionGenerator {

    public List<BuildingSection> generate(DesignBrief brief, RoofPlan roofPlan, CourtyardPlan courtyardPlan) {
        List<BuildingSection.SectionLevel> levels = new ArrayList<>();
        for (int level = 1; level <= Math.max(1, brief.floors()); level++) {
            double height = level == 1 ? 3.6 : 3.3;
            String primaryUse = level == 1 ? "living, parking/service and kitchen" : level == brief.floors() ? "bedrooms, worship/multipurpose and roof access" : "bedrooms and family room";
            levels.add(new BuildingSection.SectionLevel(level, height, 0.15, primaryUse));
        }

        BuildingSection longitudinal = new BuildingSection(
                "Longitudinal climate section",
                levels,
                "Thang đặt cạnh giếng trời để tạo ống hút khí nóng; chiếu nghỉ nhận sáng gián tiếp.",
                roofPlan.roofType() + " có mái đua " + roofPlan.overhangMeters() + "m và thoát nước về trục kỹ thuật.",
                "Skylight trên sân trong loại mở được, có lam che mưa nắng và mép thoát nhiệt cao.",
                List.of("Cao độ tầng trệt ưu tiên thoáng và linh hoạt.", "Các tầng ngủ dùng trần cao vừa phải để giảm tải điều hòa.")
        );
        BuildingSection transverse = new BuildingSection(
                "Transverse facade and ventilation section",
                levels,
                "Mặt cắt ngang thể hiện lõi thang/WC, khoảng rỗng sân trong và phòng chính hai bên.",
                "Mái tạo bóng đổ cho ban công và khe thông gió đỉnh mái.",
                "Sân trong rộng khoảng " + courtyardPlan.width() + "m x " + courtyardPlan.depth() + "m, đủ cho cây nhỏ và thông tầng.",
                List.of("Cửa sổ cao-thấp hỗ trợ gió xuyên phòng.", "Tường đặc phía nắng gắt kết hợp lam và cây xanh.")
        );
        return List.of(longitudinal, transverse);
    }
}
