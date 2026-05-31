package com.architectai.design.architecture;

import com.architectai.design.domain.DesignBrief;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FacadeCompositionEngine {

    public FacadeComposition compose(DesignBrief brief, BuildingTypology typology, ClimateAnalysis climate, ExteriorStyle style) {
        int bays = brief.siteWidthMeters() >= 8 ? 3 : brief.siteWidthMeters() >= 5.5 ? 2 : 1;
        String compositionType = bays == 1 ? "vertical townhouse composition" : bays == 2 ? "asymmetric two-bay tropical facade" : "villa facade with garden-facing bays";
        List<String> balconies = new ArrayList<>();
        List<String> fins = new ArrayList<>();
        List<String> screens = new ArrayList<>();
        List<String> green = new ArrayList<>();
        List<String> overhangs = new ArrayList<>();

        for (int level = 2; level <= Math.max(2, brief.floors()); level++) {
            balconies.add("Tầng " + level + ": ban công lệch hoặc loggia sâu 0.9-1.4m gắn với phòng ngủ/sinh hoạt chung");
        }
        if ("west".equals(brief.orientation()) || "southwest".equals(brief.orientation())) {
            fins.add("Lam đứng dày hơn ở mặt tiền hướng " + brief.orientation() + " để giảm nắng xiên buổi chiều");
            screens.add("Lớp screen thứ hai bằng gạch bông gió/nhôm giả gỗ trước kính phòng ngủ");
        } else {
            fins.add("Lam đứng mảnh tạo nhịp mặt tiền và che tầm nhìn trực diện từ phố");
            screens.add("Màn perforated hoặc gạch thông gió quanh lõi thang/giếng trời");
        }
        green.add("Bồn cây liên tục ở ban công và ô thoáng đứng");
        green.add(brief.frontYardRequired() ? "Cây bóng mát tại sân trước làm lớp đệm bụi và nắng" : "Mảng xanh treo thay sân trước nếu lô đất sát phố");
        overhangs.add("Mái đua hoặc slab canopy 0.45-0.9m tùy chiều rộng lô");
        overhangs.add("Che mưa cho cửa chính và cửa ban công bằng khung mảnh đồng bộ vật liệu");

        return new FacadeComposition(
                compositionType,
                bays,
                balconies,
                fins,
                screens,
                green,
                overhangs,
                style.facadeRules()
        );
    }
}
