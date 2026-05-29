package com.architectai.design;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RenderPromptGeneratorTest {

    private final RenderPromptGenerator generator = new RenderPromptGenerator(new VietnameseRegionalDesignGuide());

    @Test
    void generateIncludesBriefLayoutAndFloorplanContext() {
        DesignBrief brief = new DesignBrief(
                5,
                20,
                2,
                3,
                2,
                "modern",
                List.of("living", "kitchen", "bedroom"),
                List.of("small front yard"),
                List.of("west-facing facade")
        );
        LayoutPlan layoutPlan = new LayoutPlan(
                "front-to-back townhouse zoning",
                List.of("living room near facade", "kitchen and dining in the middle"),
                List.of("single stair core"),
                List.of("keep lightwell for ventilation")
        );
        Floorplan floorplan = new Floorplan(
                5,
                20,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "<svg></svg>",
                List.of(new FloorplanLevel(
                        1,
                        "Tầng 1",
                        5,
                        20,
                        List.of(
                                new FloorplanRoom("living", "Phòng khách", "living", 0, 0, 5, 4, "#fff"),
                                new FloorplanRoom("kitchen", "Bếp + ăn", "kitchen", 0, 4, 5, 4, "#fff")
                        ),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        "<svg></svg>"
                ))
        );

        String prompt = generator.generate(brief, layoutPlan, floorplan);

        assertThat(prompt)
                .contains("Vietnamese townhouse")
                .contains("5m x 20m")
                .contains("modern style")
                .contains("front-to-back townhouse zoning")
                .contains("Phòng khách")
                .contains("small front yard")
                .contains("west-facing facade")
                .contains("no text labels");
    }
}
