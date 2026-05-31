package com.architectai.design.architecture;

import com.architectai.design.domain.DesignBrief;
import com.architectai.design.layout.FloorplanGenerator;
import com.architectai.design.layout.LayoutPlan;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArchitecturePipelineTest {

    private final ArchitecturalKnowledgeBase knowledgeBase = new ArchitecturalKnowledgeBase(new ObjectMapper());
    private final TypologyEngine typologyEngine = new TypologyEngine();
    private final SpatialPatternLibrary spatialPatternLibrary = new SpatialPatternLibrary(knowledgeBase);
    private final ClimateEngine climateEngine = new ClimateEngine(knowledgeBase);
    private final CourtyardPlanner courtyardPlanner = new CourtyardPlanner();
    private final ExteriorStyleEngine exteriorStyleEngine = new ExteriorStyleEngine();
    private final RoofGenerator roofGenerator = new RoofGenerator();
    private final SectionGenerator sectionGenerator = new SectionGenerator();
    private final FacadeCompositionEngine facadeCompositionEngine = new FacadeCompositionEngine();
    private final LandscapePlanner landscapePlanner = new LandscapePlanner();
    private final ArchitecturalDrawingGenerator architecturalDrawingGenerator = new ArchitecturalDrawingGenerator();
    private final ArchitecturalDesignPackageGenerator packageGenerator = new ArchitecturalDesignPackageGenerator(
            typologyEngine,
            spatialPatternLibrary,
            climateEngine,
            courtyardPlanner,
            exteriorStyleEngine,
            roofGenerator,
            sectionGenerator,
            facadeCompositionEngine,
            landscapePlanner,
            architecturalDrawingGenerator
    );

    @Test
    void createsVietnameseArchitecturePackageBeyondFloorplan() {
        DesignBrief brief = new DesignBrief(
                5,
                22,
                3,
                4,
                3,
                "modern tropical",
                List.of("living", "kitchen", "bedroom", "courtyard"),
                List.of("Ho Chi Minh City", "green facade", "multi generation"),
                List.of(),
                "west",
                "Ho Chi Minh City",
                true,
                true,
                true,
                true,
                true,
                "middle",
                List.of("kitchen near dining", "bedroom away from street"),
                List.of(new DesignBrief.FloorRequirement(1, List.of("parking", "living", "elderly bedroom", "kitchen")))
        );

        LayoutPlan layoutPlan = new LayoutPlan("test strategy", List.of("zoning"), List.of("circulation"), List.of("notes"));
        ArchitecturalDesignPackage designPackage = packageGenerator.generate(
                brief,
                layoutPlan,
                new FloorplanGenerator().generate(brief),
                "base render prompt"
        );

        assertThat(designPackage.siteInfo().location()).isEqualTo("Ho Chi Minh City");
        assertThat(designPackage.typology().code()).isEqualTo("multi-generation-house");
        assertThat(designPackage.climateAnalysis().shadingStrategy()).anySatisfy(strategy -> assertThat(strategy).contains("west"));
        assertThat(designPackage.spatialPatterns()).extracting(SpatialPattern::code).contains("central-courtyard");
        assertThat(designPackage.courtyardPlan().width()).isGreaterThan(0);
        assertThat(designPackage.roofPlan().planes()).isNotEmpty();
        assertThat(designPackage.buildingSections()).hasSize(2);
        assertThat(designPackage.facadeComposition().balconies()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(designPackage.exteriorStyle().materialPalette()).isNotEmpty();
        assertThat(designPackage.landscapePlan().courtyardLandscape()).isNotEmpty();
        assertThat(designPackage.architecturalDrawings().exteriorPerspective().svg()).contains("<svg");
        assertThat(designPackage.architecturalDrawings().roofPlan().svg()).contains("Mặt bằng mái");
        assertThat(designPackage.architecturalDrawings().frontElevation().svg()).contains("Mặt đứng chính");
        assertThat(designPackage.architecturalDrawings().longitudinalSection().svg()).contains("Mặt cắt dọc");
        assertThat(designPackage.renderPrompts().facadePrompt()).contains("Front elevation render");
    }
}
