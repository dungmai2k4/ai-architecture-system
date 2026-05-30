package com.architectai.design.api;

import com.architectai.design.domain.DesignBrief;
import com.architectai.design.layout.Floorplan;
import com.architectai.design.layout.LayoutPlan;
import com.architectai.design.rules.RuleResult;
public record DesignResponse(
        Long projectId,
        String status,
        DesignBrief designBrief,
        RuleResult ruleResult,
        LayoutPlan layoutPlan,
        Floorplan floorplan,
        String renderPrompt,
        String renderImagePath,
        String error
) {
}
