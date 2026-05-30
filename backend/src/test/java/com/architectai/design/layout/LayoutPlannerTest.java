package com.architectai.design.layout;

import com.architectai.design.domain.DesignBrief;
import com.architectai.design.rules.VietnameseRegionalDesignGuide;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LayoutPlannerTest {

    private final LayoutPlanner planner = new LayoutPlanner(new VietnameseRegionalDesignGuide());

    @Test
    void planIncludesExtractedLayoutIntentSignals() {
        DesignBrief brief = new DesignBrief(
                5,
                20,
                3,
                4,
                3,
                "modern",
                List.of("living", "kitchen", "bedroom", "garage"),
                List.of("northern Vietnam style"),
                List.of(),
                "west",
                true,
                true,
                true,
                true,
                true,
                "middle",
                List.of("kitchen near dining", "WC not facing kitchen"),
                List.of(
                        new DesignBrief.FloorRequirement(1, List.of("parking", "living", "kitchen")),
                        new DesignBrief.FloorRequirement(2, List.of("bedroom", "bedroom", "bathroom"))
                )
        );

        LayoutPlan plan = planner.plan(brief);

        assertThat(plan.circulation()).contains("Đặt cụm thang bộ, WC và thang máy (nếu có) ở giữa nhà để chia nhịp trước-sau và rút ngắn giao thông.");
        assertThat(plan.notes())
                .anySatisfy(note -> assertThat(note).contains("Hướng nhà west"))
                .anySatisfy(note -> assertThat(note).contains("nhu cầu để xe"))
                .anySatisfy(note -> assertThat(note).contains("giếng trời/sân trong"))
                .anySatisfy(note -> assertThat(note).contains("Quan hệ phòng cần ưu tiên", "kitchen near dining"))
                .anySatisfy(note -> assertThat(note).contains("Yêu cầu theo tầng", "tầng 1 gồm parking, living, kitchen"));
    }
}
