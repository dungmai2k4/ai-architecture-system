package com.architectai.design.architecture;

import com.architectai.design.layout.Floorplan;

import java.util.List;

public record ArchitecturalDesignPackage(
        SiteInfo siteInfo,
        BuildingTypology typology,
        ClimateAnalysis climateAnalysis,
        List<SpatialPattern> spatialPatterns,
        CourtyardPlan courtyardPlan,
        Floorplan floorPlans,
        RoofPlan roofPlan,
        List<BuildingSection> buildingSections,
        FacadeComposition facadeComposition,
        ExteriorStyle exteriorStyle,
        LandscapePlan landscapePlan,
        RenderPrompts renderPrompts
) {
}
