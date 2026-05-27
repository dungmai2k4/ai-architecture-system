package com.architectai.design;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LayoutPlanner {

    public LayoutPlan plan(DesignBrief brief) {
        List<String> zoning = new ArrayList<>();
        zoning.add("Tầng 1: phòng khách phía trước, bếp + ăn ở giữa, WC/giặt ở cuối nhà.");

        if (brief.floors() >= 2) {
            zoning.add("Tầng 2: ưu tiên khối ngủ chính + WC dùng chung, giữ thông tầng hoặc giếng trời giữa nhà.");
        }

        if (brief.floors() >= 3) {
            zoning.add("Tầng trên cùng: phòng thờ/đa năng phía trước, sân phơi phía sau.");
        }

        List<String> circulation = new ArrayList<>();
        circulation.add("Đặt thang sát một biên tường để giảm diện tích giao thông.");
        circulation.add("Giữ hành lang ngắn, liên thông khách - bếp để tăng cảm giác rộng.");

        List<String> notes = new ArrayList<>();
        if (brief.siteWidthMeters() < 4) {
            notes.add("Mặt tiền hẹp: dùng đồ nội thất âm tường và cửa lùa để tiết kiệm diện tích mở cánh.");
        }
        if (brief.siteDepthMeters() >= 20) {
            notes.add("Nhà sâu: cân nhắc giếng trời giữa nhà để cải thiện thông gió và chiếu sáng.");
        }

        String strategy = brief.siteWidthMeters() >= 5
                ? "Phân khu 3 lớp trước - giữa - sau"
                : "Phân khu 2 lớp với lõi giao thông áp tường";

        return new LayoutPlan(strategy, zoning, circulation, notes);
    }
}
