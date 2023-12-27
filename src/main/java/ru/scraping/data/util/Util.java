package ru.scraping.data.util;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Util {

    private Util() {

    }

    private static final Pattern FLOAT_PATTERN = Pattern.compile("\\d+[,.]?\\d*");

    public static BigDecimal parseFloats(String raw) {
        Matcher m = FLOAT_PATTERN.matcher(raw);
        if (m.find()) {
            return new BigDecimal(m.group().replace(",", "."));
        } else {
            return null;
        }
    }
}
