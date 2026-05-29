package com.architectai.design;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LayoutPlanner {

    private final VietnameseRegionalDesignGuide regionalDesignGuide;

    public LayoutPlanner(VietnameseRegionalDesignGuide regionalDesignGuide) {
        this.regionalDesignGuide = regionalDesignGuide;
    }

    public LayoutPlan plan(DesignBrief brief) {
        VietnameseRegionalDesignGuide.RegionalDesignProfile profile = regionalDesignGuide.resolve(brief);
        List<String> zoning = new ArrayList<>();
        zoning.add("Tầng 1: sân/hiên đệm phía trước, phòng khách, lõi thang + WC + giếng trời ở giữa, bếp ăn mở về sân sau.");

        if (brief.floors() >= 2) {
            zoning.add("Tầng 2: phòng ngủ trước/sau tách bởi sinh hoạt chung và lõi thang, WC bám lõi kỹ thuật để gom đường ống.");
        }

        if (brief.floors() >= 3) {
            zoning.add("Tầng trên cùng: phòng thờ/đa năng yên tĩnh phía trước, giặt phơi và vườn mái phía sau.");
        }

        List<String> circulation = new ArrayList<>();
        circulation.add("Đặt thang sát một biên tường và gom WC cạnh thang để giảm diện tích giao thông, gom trục kỹ thuật.");
        circulation.add("Tạo trục đi thẳng, ít hành lang; khách - lõi giữa - bếp liên thông bằng cửa trượt hoặc khoảng đệm.");
        circulation.add("Bố trí cửa sổ/ô thoáng trước, giếng trời giữa và sân sau để hình thành thông gió chéo.");

        List<String> notes = new ArrayList<>();
        notes.add("Định hướng vùng miền: " + profile.typology() + " - " + profile.materialPalette() + ".");
        notes.addAll(profile.layoutPrinciples());
        if (brief.siteWidthMeters() < 4) {
            notes.add("Mặt tiền hẹp: dùng đồ nội thất âm tường, cửa lùa và thang một vế/đổi chiều gọn để tiết kiệm diện tích mở cánh.");
        }
        if (brief.siteDepthMeters() >= 20) {
            notes.add("Nhà sâu: cân nhắc giếng trời giữa nhà để cải thiện thông gió và chiếu sáng.");
        }

        String strategy = brief.siteWidthMeters() >= 5
                ? "Phân khu 3 lớp trước - lõi xanh giữa - sau theo " + profile.region()
                : "Nhà ống hẹp: lõi thang/WC áp tường, giếng trời giữa và sân sau theo " + profile.region();

        return new LayoutPlan(strategy, zoning, circulation, notes);
    }
}
