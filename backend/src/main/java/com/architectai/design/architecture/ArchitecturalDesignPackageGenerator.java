package com.architectai.design.architecture;

import com.architectai.design.domain.DesignBrief;
import com.architectai.design.layout.Floorplan;
import com.architectai.design.layout.LayoutPlan;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ArchitecturalDesignPackageGenerator {

    private final TypologyEngine typologyEngine;
    private final SpatialPatternLibrary spatialPatternLibrary;
    private final ClimateEngine climateEngine;
    private final CourtyardPlanner courtyardPlanner;
    private final ExteriorStyleEngine exteriorStyleEngine;
    private final RoofGenerator roofGenerator;
    private final SectionGenerator sectionGenerator;
    private final FacadeCompositionEngine facadeCompositionEngine;
    private final LandscapePlanner landscapePlanner;

    public ArchitecturalDesignPackageGenerator(
            TypologyEngine typologyEngine,
            SpatialPatternLibrary spatialPatternLibrary,
            ClimateEngine climateEngine,
            CourtyardPlanner courtyardPlanner,
            ExteriorStyleEngine exteriorStyleEngine,
            RoofGenerator roofGenerator,
            SectionGenerator sectionGenerator,
            FacadeCompositionEngine facadeCompositionEngine,
            LandscapePlanner landscapePlanner
    ) {
        this.typologyEngine = typologyEngine;
        this.spatialPatternLibrary = spatialPatternLibrary;
        this.climateEngine = climateEngine;
        this.courtyardPlanner = courtyardPlanner;
        this.exteriorStyleEngine = exteriorStyleEngine;
        this.roofGenerator = roofGenerator;
        this.sectionGenerator = sectionGenerator;
        this.facadeCompositionEngine = facadeCompositionEngine;
        this.landscapePlanner = landscapePlanner;
    }

    public ArchitecturalDesignPackage generate(DesignBrief brief, LayoutPlan layoutPlan, Floorplan floorplan, String baseRenderPrompt) {
        SiteInfo siteInfo = new SiteInfo(
                brief.siteWidthMeters(),
                brief.siteDepthMeters(),
                round(brief.siteWidthMeters() * brief.siteDepthMeters()),
                brief.location(),
                brief.orientation(),
                resolveUrbanContext(brief)
        );
        BuildingTypology typology = typologyEngine.select(brief);
        ClimateAnalysis climate = climateEngine.analyze(brief);
        List<SpatialPattern> patterns = spatialPatternLibrary.selectPatterns(brief, typology);
        CourtyardPlan courtyard = courtyardPlanner.plan(brief, typology, patterns, climate);
        ExteriorStyle exteriorStyle = exteriorStyleEngine.generate(brief, typology, climate);
        RoofPlan roofPlan = roofGenerator.generate(brief, typology, climate, exteriorStyle);
        List<BuildingSection> sections = sectionGenerator.generate(brief, roofPlan, courtyard);
        FacadeComposition facade = facadeCompositionEngine.compose(brief, typology, climate, exteriorStyle);
        LandscapePlan landscape = landscapePlanner.plan(brief, courtyard, exteriorStyle);
        RenderPrompts prompts = createRenderPrompts(baseRenderPrompt, typology, climate, courtyard, roofPlan, facade, exteriorStyle, landscape, layoutPlan);

        return new ArchitecturalDesignPackage(
                siteInfo,
                typology,
                climate,
                patterns,
                courtyard,
                floorplan,
                roofPlan,
                sections,
                facade,
                exteriorStyle,
                landscape,
                prompts
        );
    }

    private RenderPrompts createRenderPrompts(
            String baseRenderPrompt,
            BuildingTypology typology,
            ClimateAnalysis climate,
            CourtyardPlan courtyard,
            RoofPlan roofPlan,
            FacadeComposition facade,
            ExteriorStyle style,
            LandscapePlan landscape,
            LayoutPlan layoutPlan
    ) {
        String exterior = baseRenderPrompt
                + " Exterior concept: " + typology.name()
                + ", " + style.name()
                + ", facade composition " + facade.compositionType()
                + ", materials " + String.join(", ", style.materialPalette())
                + ", climate strategy " + String.join(", ", climate.shadingStrategy())
                + ", lush but realistic Vietnamese tropical landscape.";
        String courtyardPrompt = "Photorealistic interior courtyard view for a Vietnamese " + typology.name()
                + ": " + courtyard.type() + " courtyard " + courtyard.width() + "m x " + courtyard.depth()
                + "m, tree placement " + String.join(", ", courtyard.treePlacement())
                + ", skylight " + String.join(", ", courtyard.skylightPlacement())
                + ", calm daylight, no people, no text.";
        String facadePrompt = "Front elevation render: " + facade.compositionType()
                + ", balconies " + String.join(", ", facade.balconies())
                + ", vertical fins " + String.join(", ", facade.verticalFins())
                + ", screens " + String.join(", ", facade.sunScreens())
                + ", greenery " + String.join(", ", facade.greenFacade())
                + ", modern Vietnamese tropical architecture.";
        String roofSectionPrompt = "Architectural roof and section diagram render: " + roofPlan.roofType()
                + ", slope " + roofPlan.slopeDegrees()
                + " degrees, section strategy " + String.join(", ", layoutPlan.circulation())
                + ", show stair void, skylight, roof overhang and passive ventilation arrows, clean professional style.";
        return new RenderPrompts(exterior, courtyardPrompt, facadePrompt, roofSectionPrompt);
    }

    private String resolveUrbanContext(DesignBrief brief) {
        String text = DesignText.combined(brief);
        if (DesignText.containsAny(text, "rural", "nông thôn", "nong thon", "nhà vườn", "nha vuon")) {
            return "rural residential plot";
        }
        if (DesignText.containsAny(text, "riverside", "ven sông", "ven song")) {
            return "riverside residential plot";
        }
        if (brief.siteWidthMeters() < 6) {
            return "dense Vietnamese urban townhouse lot";
        }
        return "Vietnamese residential plot with garden potential";
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
