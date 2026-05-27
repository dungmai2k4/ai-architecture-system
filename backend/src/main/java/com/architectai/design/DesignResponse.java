package com.architectai.design;

public record DesignResponse(
        Long projectId,
        String status,
        DesignBrief designBrief,
        RuleResult ruleResult,
        String error
) {
}
