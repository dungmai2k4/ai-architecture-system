package com.architectai.design.rules;

import com.architectai.design.domain.DesignBrief;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VietnameseRegionalDesignGuideTest {

    private final VietnameseRegionalDesignGuide guide = new VietnameseRegionalDesignGuide();

    @Test
    void resolveDetectsRegionalSignalsFromPreferences() {
        DesignBrief brief = new DesignBrief(
                5,
                20,
                3,
                4,
                3,
                "modern",
                List.of("living", "kitchen"),
                List.of("thiết kế kiểu Huế miền Trung, có sân trong"),
                List.of()
        );

        VietnameseRegionalDesignGuide.RegionalDesignProfile profile = guide.resolve(brief);

        assertThat(profile.region()).isEqualTo("duyên hải miền Trung");
        assertThat(profile.materialPalette()).contains("mái dốc nhẹ");
        assertThat(profile.layoutPrinciples()).isNotEmpty();
    }
}
