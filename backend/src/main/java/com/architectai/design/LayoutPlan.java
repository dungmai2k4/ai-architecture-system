package com.architectai.design;

import java.util.List;

public record LayoutPlan(
        String strategy,
        List<String> zoning,
        List<String> circulation,
        List<String> notes
) {
}
