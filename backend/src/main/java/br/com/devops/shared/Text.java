package br.com.devops.shared;

import java.text.Normalizer;
import java.util.Locale;

public final class Text {
    private Text() {}

    public static String clean(String value) {
        return value == null ? "" : value.strip();
    }

    public static String normalize(String value) {
        return Normalizer.normalize(clean(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
