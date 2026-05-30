package com.architectai.design.rules;

import com.architectai.design.domain.DesignBrief;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class VietnameseRegionalDesignGuide {

    public RegionalDesignProfile resolve(DesignBrief brief) {
        String text = String.join(" ", collectSignals(brief)).toLowerCase(Locale.ROOT);

        if (containsAny(text, "hue", "huế", "da nang", "đà nẵng", "hoi an", "hội an", "mien trung", "miền trung", "nha trang", "quy nhon", "quy nhơn")) {
            return new RegionalDesignProfile(
                    "duyên hải miền Trung",
                    "nhà vườn Huế/nhà phố chống bão miền Trung",
                    List.of(
                            "Tổ chức lõi nhà chắc, khoảng đệm hiên hoặc sân trong để giảm nắng gắt và mưa tạt.",
                            "Ưu tiên mái dốc, lam che mưa nắng, vật liệu gạch/đá/terrazzo bền ẩm mặn.",
                            "Không gian thờ hoặc sinh hoạt chung nên đặt trang trọng ở tầng trên hoặc phía trước, tránh luồng bếp-WC trực diện."
                    ),
                    "mái dốc nhẹ, hiên sâu, gạch nung/đá rửa, lam che bão nắng, sân trong xanh"
            );
        }

        if (containsAny(text, "sai gon", "sài gòn", "tp hcm", "ho chi minh", "hồ chí minh", "mien nam", "miền nam", "mekong", "miền tây", "can tho", "cần thơ", "dong nai", "đồng nai", "binh duong", "bình dương")) {
            return new RegionalDesignProfile(
                    "miền Nam",
                    "nhà nhiệt đới Nam Bộ",
                    List.of(
                            "Tăng không gian bán ngoài trời như hiên, ban công và sân sau để phù hợp lối sống thoáng mở.",
                            "Dùng cửa/louver lớn, thông gió chéo và mảng xanh để giảm ẩm nóng quanh năm.",
                            "Bếp + ăn có thể mở về sân sau nhưng vẫn cần tách mùi bằng cửa trượt hoặc khoảng đệm cây xanh."
                    ),
                    "hiên rộng, lam gỗ/kim loại, gạch bông hoặc terrazzo, cây nhiệt đới, màu sáng thoáng"
            );
        }

        if (containsAny(text, "ha noi", "hà nội", "mien bac", "miền bắc", "bac bo", "bắc bộ", "red river", "đồng bằng bắc bộ", "hai phong", "hải phòng", "nam dinh", "nam định")) {
            return new RegionalDesignProfile(
                    "miền Bắc",
                    "nhà phố Bắc Bộ đương đại",
                    List.of(
                            "Giữ trục đón khách trang trọng phía trước, khoảng đệm sân/hiên giúp chuyển tiếp từ phố vào nhà.",
                            "Tận dụng giếng trời giữa nhà để lấy sáng mùa nồm ẩm và tạo đối lưu cho lô đất sâu.",
                            "Có thể dùng gợi ý mái ngói, gạch mộc, gỗ ấm và sân nhỏ kiểu nhà sân vườn Bắc Bộ."
                    ),
                    "gạch mộc, ngói hoặc mái dốc hiện đại, gỗ ấm, sân/hiên trước, giếng trời trung tâm"
            );
        }

        return new RegionalDesignProfile(
                "Việt Nam đương đại",
                "nhà ống Việt Nam thích ứng khí hậu",
                List.of(
                        "Ưu tiên mặt bằng trước-giữa-sau rõ ràng: tiếp khách ở trước, lõi thang/WC/giếng trời ở giữa, bếp ăn mở về sau.",
                        "Lô đất hẹp và sâu cần giếng trời hoặc sân trong để đưa sáng và gió vào giữa nhà.",
                        "Mặt đứng nên dùng lam, ban công cây xanh và vật liệu địa phương để cân bằng riêng tư với thông thoáng."
                ),
                "lam che nắng, ban công cây xanh, gạch/terrazzo, giếng trời, ánh sáng tự nhiên"
        );
    }

    private List<String> collectSignals(DesignBrief brief) {
        List<String> values = new ArrayList<>();
        if (brief.style() != null) {
            values.add(brief.style());
        }
        if (brief.preferences() != null) {
            values.addAll(brief.preferences());
        }
        if (brief.constraints() != null) {
            values.addAll(brief.constraints());
        }
        if (brief.rooms() != null) {
            values.addAll(brief.rooms());
        }
        return values;
    }

    private boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    public record RegionalDesignProfile(
            String region,
            String typology,
            List<String> layoutPrinciples,
            String materialPalette
    ) {
    }
}
