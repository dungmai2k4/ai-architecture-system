package com.architectai.design;

import jakarta.validation.constraints.NotBlank;

public record CreateDesignRequest(
        @NotBlank(message = "requirement is required")
        String requirement
) {
}
