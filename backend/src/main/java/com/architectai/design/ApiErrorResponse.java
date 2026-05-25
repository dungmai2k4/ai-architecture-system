package com.architectai.design;

public record ApiErrorResponse(
        String error,
        String code
) {
}
