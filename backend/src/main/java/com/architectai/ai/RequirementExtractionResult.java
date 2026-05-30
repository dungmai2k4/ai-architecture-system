package com.architectai.ai;

import com.architectai.design.domain.DesignBrief;

public record RequirementExtractionResult(DesignBrief designBrief, String rawResponse) {
}
