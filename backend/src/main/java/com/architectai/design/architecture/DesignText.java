package com.architectai.design.architecture;

import com.architectai.design.domain.DesignBrief;

import java.text.Normalizer;
import java.util.Locale;

final class DesignText {

    private DesignText() {
    }

    static String combined(DesignBrief brief) {
        return normalize(String.join(" ",
                safe(brief.style()),
                safe(brief.location()),
                String.join(" ", brief.rooms()),
                String.join(" ", brief.preferences()),
                String.join(" ", brief.constraints()),
                String.join(" ", brief.adjacencyPreferences())
        ));
    }

    static boolean containsAny(String text, String... needles) {
        String normalizedText = normalize(text);
        for (String needle : needles) {
            if (normalizedText.contains(normalize(needle))) {
                return true;
            }
        }
        return false;
    }

    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
