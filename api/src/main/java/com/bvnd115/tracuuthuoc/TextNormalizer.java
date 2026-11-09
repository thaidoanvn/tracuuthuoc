package com.bvnd115.tracuuthuoc;

import java.text.Normalizer;
import java.util.Locale;

public final class TextNormalizer {

    private TextNormalizer() {
    }

    public static String fold(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String replaced = value.replace('đ', 'd').replace('Đ', 'd');
        String decomposed = Normalizer.normalize(replaced, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).trim();
    }

    public static String escapeLike(String folded) {
        return folded
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
