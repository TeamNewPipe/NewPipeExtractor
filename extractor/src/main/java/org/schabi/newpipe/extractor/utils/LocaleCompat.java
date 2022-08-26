package org.schabi.newpipe.extractor.utils;

import java.util.Locale;

public class LocaleCompat {
    private LocaleCompat() {
    }

    // Source: LocaleListCompat's private forLanguageTagCompat() method.
    // Use Locale.forLanguageTag() on API level >= 21 instead.
    public static Locale forLanguageTag(final String str) {
        if (str.contains("-")) {
            String[] args = str.split("-", -1);
            if (args.length > 2) {
                return new Locale(args[0], args[1], args[2]);
            } else if (args.length > 1) {
                return new Locale(args[0], args[1]);
            } else if (args.length == 1) {
                return new Locale(args[0]);
            }
        } else if (str.contains("_")) {
            String[] args = str.split("_", -1);
            if (args.length > 2) {
                return new Locale(args[0], args[1], args[2]);
            } else if (args.length > 1) {
                return new Locale(args[0], args[1]);
            } else if (args.length == 1) {
                return new Locale(args[0]);
            }
        } else {
            return new Locale(str);
        }

        throw new IllegalArgumentException("Can not parse language tag: [" + str + "]");
    }
}
