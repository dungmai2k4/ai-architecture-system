package com.architectai.ai;

import com.architectai.design.DesignBrief;

public record RequirementExtractionResult(DesignBrief designBrief, String rawResponse) {
}
