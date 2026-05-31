package com.architectai.design.architecture;

import com.architectai.design.domain.DesignBrief;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LandscapePlanner {

    public LandscapePlan plan(DesignBrief brief, CourtyardPlan courtyardPlan, ExteriorStyle style) {
        return new LandscapePlan(
                brief.frontYardRequired() || brief.parkingRequired()
                        ? List.of("sân trước lát thấm nước kết hợp bồn cây thấp", "cây bóng mát nhỏ không cản cửa xe", "hiên chuyển tiếp giảm bụi/ồn")
                        : List.of("bồn cây sát mặt tiền", "cửa chính lùi nhẹ tạo khoảng đệm", "chậu cây đứng cạnh lam che nắng"),
                List.of("cây tán nhỏ trong sân " + courtyardPlan.type(), "mặt nước nhỏ: " + (courtyardPlan.waterFeature() ? "có" : "không bắt buộc"), "sỏi hoặc gạch nung thoát nước nhanh"),
                brief.rearGardenRequired() ? List.of("vườn sau kết hợp giặt phơi kín đáo", "cây gia vị gần bếp", "mảng xanh che tường hậu") : List.of("sân kỹ thuật nhỏ có lam che", "bồn cây dọc tường sau"),
                List.of("cây rủ ban công", "bồn cây có chống thấm và thoát nước riêng", "ưu tiên cây chịu nắng nếu hướng tây"),
                style.landscapeRules()
        );
    }
}
