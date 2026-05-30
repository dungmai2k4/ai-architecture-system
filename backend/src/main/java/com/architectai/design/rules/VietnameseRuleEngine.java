package com.architectai.design.rules;

import com.architectai.design.domain.DesignBrief;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VietnameseRuleEngine {

    private final VietnameseRegionalDesignGuide regionalDesignGuide;

    public VietnameseRuleEngine(VietnameseRegionalDesignGuide regionalDesignGuide) {
        this.regionalDesignGuide = regionalDesignGuide;
    }

    public RuleResult evaluate(DesignBrief designBrief) {
        List<String> warnings = new ArrayList<>();
        VietnameseRegionalDesignGuide.RegionalDesignProfile profile = regionalDesignGuide.resolve(designBrief);

        double area = designBrief.siteWidthMeters() * designBrief.siteDepthMeters();
        if (area < 36) {
            warnings.add("Diện tích lô đất dưới 36m², cần ưu tiên giải pháp thông thoáng và tối ưu công năng.");
        }

        if (designBrief.floors() >= 3 && designBrief.siteWidthMeters() < 4) {
            warnings.add("Nhà từ 3 tầng trở lên với mặt tiền dưới 4m nên kiểm tra kỹ lõi giao thông và ánh sáng tự nhiên.");
        }

        if (designBrief.bedrooms() >= 4 && designBrief.bathrooms() < 2) {
            warnings.add("Số phòng ngủ cao nhưng số WC thấp, có thể gây quá tải giờ cao điểm.");
        }

        if (designBrief.siteDepthMeters() >= 20 && !designBrief.lightwellRequired() && !containsRoom(designBrief.rooms(), "courtyard")) {
            warnings.add("Lô đất sâu từ 20m trở lên nên cân nhắc giếng trời hoặc sân trong để tăng thông gió.");
        }

        if (designBrief.parkingRequired() && designBrief.siteWidthMeters() < 3.8) {
            warnings.add("Mặt tiền dưới 3.8m có nhu cầu để xe, cần kiểm tra bán kính quay xe và khoảng mở cửa an toàn.");
        }

        if (designBrief.openKitchen() && designBrief.bathrooms() > 0) {
            warnings.add("Bếp mở cần tránh cửa WC nhìn trực tiếp vào khu bếp-ăn và nên có thông gió độc lập.");
        }

        warnings.add("Gợi ý bản sắc " + profile.region() + ": " + profile.layoutPrinciples().get(0));

        return new RuleResult(warnings);
    }

    private boolean containsRoom(List<String> rooms, String value) {
        return rooms != null && rooms.stream().anyMatch(room -> value.equalsIgnoreCase(room));
    }
}
