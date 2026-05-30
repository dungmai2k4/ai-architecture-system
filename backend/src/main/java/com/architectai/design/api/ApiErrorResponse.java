package com.architectai.design.api;

public record ApiErrorResponse(
        String error,
        String code
) {
}
