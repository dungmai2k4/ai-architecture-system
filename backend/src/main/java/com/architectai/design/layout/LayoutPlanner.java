package com.architectai.design.layout;

import com.architectai.design.domain.DesignBrief;
import com.architectai.design.rules.VietnameseRegionalDesignGuide;
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
        zoning.add("Tầng 1: tổ chức lệch trục với sân/hiên trước, phòng khách, sân trong/giếng trời, cụm thang-WC và bếp ăn; ưu tiên chia mảng rộng-hẹp thay vì các khoang ngang đều nhau.");

        if (brief.floors() >= 2) {
            zoning.add("Tầng 2: phòng ngủ trước/sau kết hợp góc học hoặc thay đồ, sinh hoạt chung và ô thoáng đặt lệch để tạo mặt bằng đa dạng.");
        }

        if (brief.floors() >= 3) {
            zoning.add("Tầng trên cùng: phòng thờ/đa năng yên tĩnh phía trước, giặt phơi và vườn mái/sân thượng phía sau.");
        }

        if (brief.floors() >= 4) {
            zoning.add("Nhà từ 4 tầng trở lên: bổ sung thang máy cạnh thang bộ để dùng chung lõi giao thông và trục kỹ thuật.");
        }

        List<String> circulation = new ArrayList<>();
        circulation.add(describeStairStrategy(brief));
        circulation.add("Tạo luồng đi theo nhịp sân trước - phòng khách - sân trong/ô thoáng - bếp/sân sau, có các nút mở bên hông thay vì một đường thẳng duy nhất.");
        circulation.add("Bố trí cửa sổ/ô thoáng trước, sân trong lệch tâm và sân sau/vườn mái để hình thành thông gió chéo.");

        List<String> notes = new ArrayList<>();
        notes.add("Định hướng vùng miền: " + profile.typology() + " - " + profile.materialPalette() + ".");
        notes.addAll(profile.layoutPrinciples());
        notes.addAll(describeLayoutIntent(brief));
        if (brief.siteWidthMeters() < 4) {
            notes.add("Mặt tiền hẹp: dùng đồ nội thất âm tường, cửa lùa và thang một vế/đổi chiều gọn để tiết kiệm diện tích mở cánh.");
        }
        if (brief.siteDepthMeters() >= 20) {
            notes.add("Nhà sâu: dùng sân trong/giếng trời lệch tâm để chia nhịp không gian, tránh chuỗi phòng có chiều rộng giống nhau.");
        }
        if (brief.floors() >= 4) {
            notes.add("Từ 4 tầng: bố trí thang máy tối thiểu cạnh thang bộ, dùng chung sảnh chờ và lõi kỹ thuật để không phá vỡ thông gió tự nhiên.");
        }

        String strategy = brief.siteWidthMeters() >= 6
                ? "Mặt bằng biệt thự/nhà phố rộng: chia cánh trái-phải với sân trong, lõi giao thông lệch và sân sau theo " + profile.region()
                : "Nhà ống hẹp: mặt bằng lệch trục với lõi thang/WC/thang máy áp tường, sân trong và sân sau theo " + profile.region();

        return new LayoutPlan(strategy, zoning, circulation, notes);
    }

    private String describeStairStrategy(DesignBrief brief) {
        return switch (brief.stairPreference()) {
            case "front" -> "Ưu tiên lõi thang gần phía trước theo yêu cầu, nhưng cần tạo khoảng đệm để không cắt trực diện phòng khách.";
            case "middle" -> "Đặt cụm thang bộ, WC và thang máy (nếu có) ở giữa nhà để chia nhịp trước-sau và rút ngắn giao thông.";
            case "rear" -> "Ưu tiên lõi thang cuối nhà theo yêu cầu, đồng thời giữ đường đi rõ từ phòng khách đến bếp và sân sau.";
            case "side" -> "Đặt cụm thang bộ, WC và thang máy (nếu có) lệch một bên để giải phóng các phòng chính và tránh hành lang dài đơn điệu.";
            default -> "Đặt cụm thang bộ, WC và thang máy (nếu có) thành lõi lệch một bên để giải phóng các phòng chính và tránh hành lang dài đơn điệu.";
        };
    }

    private List<String> describeLayoutIntent(DesignBrief brief) {
        List<String> intent = new ArrayList<>();
        if (!"unknown".equals(brief.orientation())) {
            intent.add("Hướng nhà " + brief.orientation() + ": cần kiểm soát nắng, riêng tư mặt tiền và vị trí mở cửa theo hướng này.");
        }
        if (brief.parkingRequired()) {
            intent.add("Có nhu cầu để xe: tầng 1 cần chừa khoảng đệm phía trước hoặc gara trước khi vào phòng khách.");
        }
        if (brief.lightwellRequired()) {
            intent.add("Có yêu cầu giếng trời/sân trong: ưu tiên đặt ô thoáng lệch tâm để tăng sáng và thông gió chéo.");
        }
        if (brief.frontYardRequired()) {
            intent.add("Có yêu cầu sân trước/hiên: dùng làm khoảng chuyển tiếp từ phố vào nhà và giảm ồn bụi.");
        }
        if (brief.rearGardenRequired()) {
            intent.add("Có yêu cầu sân sau/vườn sau: bếp ăn và giặt phơi nên mở về vùng thoáng phía sau.");
        }
        if (brief.openKitchen()) {
            intent.add("Có yêu cầu bếp mở: liên kết bếp-ăn với phòng khách bằng khoảng đệm thoáng, vẫn kiểm soát mùi bằng cửa trượt hoặc thông gió.");
        }
        if (!brief.adjacencyPreferences().isEmpty()) {
            intent.add("Quan hệ phòng cần ưu tiên: " + String.join("; ", brief.adjacencyPreferences()) + ".");
        }
        if (!brief.floorRequirements().isEmpty()) {
            intent.add("Yêu cầu theo tầng: " + describeFloorRequirements(brief.floorRequirements()) + ".");
        }
        return intent;
    }

    private String describeFloorRequirements(List<DesignBrief.FloorRequirement> floorRequirements) {
        List<String> descriptions = new ArrayList<>();
        for (DesignBrief.FloorRequirement requirement : floorRequirements) {
            descriptions.add("tầng " + requirement.level() + " gồm " + String.join(", ", requirement.rooms()));
        }
        return String.join("; ", descriptions);
    }
}
