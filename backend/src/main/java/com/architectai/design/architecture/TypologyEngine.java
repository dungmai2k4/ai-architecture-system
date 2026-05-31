package com.architectai.design.architecture;

import com.architectai.design.domain.DesignBrief;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TypologyEngine {

    public BuildingTypology select(DesignBrief brief) {
        String text = DesignText.combined(brief);
        double width = brief.siteWidthMeters();
        double depth = brief.siteDepthMeters();
        String code;
        String name;
        String description;
        List<String> patterns;

        if (DesignText.containsAny(text, "riverside", "ven song", "ven sông", "mekong", "mien tay", "miền tây")) {
            code = "riverside-house";
            name = "Riverside House";
            description = "Nhà ven sông nâng cao trải nghiệm hiên nước, thông gió ẩm và khoảng đệm xanh.";
            patterns = List.of("side-courtyard", "l-shaped-layout");
        } else if (DesignText.containsAny(text, "nhieu the he", "nhiều thế hệ", "ong ba", "ông bà", "elderly", "grandparent")) {
            code = "multi-generation-house";
            name = "Multi Generation House";
            description = "Nhà nhiều thế hệ cân bằng phòng riêng, không gian sinh hoạt chung và phòng người cao tuổi tầng trệt.";
            patterns = List.of("multi-generation-cluster", "central-courtyard");
        } else if (DesignText.containsAny(text, "chu u", "chữ u", "u-shaped", "u shaped")) {
            code = "rural-u-shaped-house";
            name = "Rural U-Shaped House";
            description = "Nhà chữ U tạo sân gia đình được che chở bởi ba cánh nhà.";
            patterns = List.of("u-shaped-layout");
        } else if (DesignText.containsAny(text, "chu l", "chữ l", "l-shaped", "l shaped", "nha vuon", "nhà vườn") && width >= 7) {
            code = "rural-l-shaped-house";
            name = "Rural L-Shaped House";
            description = "Nhà chữ L mở về sân vườn, phù hợp lô đất rộng hoặc bối cảnh nông thôn.";
            patterns = List.of("l-shaped-layout");
        } else if (DesignText.containsAny(text, "biet thu", "biệt thự", "villa", "resort") && DesignText.containsAny(text, "tropical", "nhiet doi", "nhiệt đới", "san vuon", "sân vườn")) {
            code = "tropical-villa";
            name = "Tropical Villa";
            description = "Biệt thự nhiệt đới ưu tiên hiên sâu, mảng xanh và không gian bán ngoài trời.";
            patterns = List.of("l-shaped-layout", "u-shaped-layout", "front-courtyard");
        } else if (DesignText.containsAny(text, "biet thu", "biệt thự", "villa") || width >= 8) {
            code = "modern-villa";
            name = "Modern Villa";
            description = "Biệt thự hiện đại với khối tích rõ, ban công rộng và sân vườn kiểm soát nắng.";
            patterns = List.of("front-courtyard", "double-loaded-corridor");
        } else if (brief.lightwellRequired() || depth >= 20 || DesignText.containsAny(text, "san trong", "sân trong", "courtyard", "gieng troi", "giếng trời")) {
            code = "courtyard-house";
            name = "Courtyard House";
            description = "Nhà sân trong dùng khoảng rỗng xanh để lấy sáng, thông gió và chia nhịp nhà sâu.";
            patterns = List.of("central-courtyard", "side-courtyard");
        } else {
            code = "townhouse";
            name = "Townhouse";
            description = "Nhà phố Việt Nam theo lô hẹp sâu, tổ chức lõi thang/WC và ô thoáng hiệu quả.";
            patterns = List.of("side-courtyard", "central-courtyard");
        }

        return new BuildingTypology(
                code,
                name,
                description,
                fitReasons(brief, name),
                planningPriorities(brief, code),
                patterns
        );
    }

    private List<String> fitReasons(DesignBrief brief, String name) {
        return List.of(
                "Kích thước lô " + brief.siteWidthMeters() + "m x " + brief.siteDepthMeters() + "m phù hợp nhóm " + name + ".",
                "Số tầng " + Math.max(1, brief.floors()) + " và " + brief.bedrooms() + " phòng ngủ được cân bằng với lõi giao thông đứng.",
                brief.lightwellRequired() ? "Yêu cầu giếng trời/sân trong là tín hiệu chính cho tổ chức vi khí hậu." : "Chưa có yêu cầu sân trong rõ, hệ thống chọn mẫu thích ứng khí hậu mặc định."
        );
    }

    private List<String> planningPriorities(DesignBrief brief, String code) {
        if ("multi-generation-house".equals(code)) {
            return List.of("phòng người cao tuổi tầng trệt", "sinh hoạt chung rộng", "riêng tư giữa các thế hệ", "đường đi ngắn và ít bậc");
        }
        if ("riverside-house".equals(code)) {
            return List.of("hiên nhìn sông", "nền và sân có lớp đệm chống ẩm", "gió xuyên phòng", "vật liệu chịu ẩm");
        }
        if (code.contains("villa") || code.startsWith("rural")) {
            return List.of("hiên sâu", "sân vườn kết nối phòng chính", "mái đua lớn", "phân tách khách-ngủ-dịch vụ");
        }
        return List.of("lõi thang gọn", "giếng trời hoặc ô thoáng", "bếp mở về sân sau", "mặt đứng che nắng và riêng tư");
    }
}
