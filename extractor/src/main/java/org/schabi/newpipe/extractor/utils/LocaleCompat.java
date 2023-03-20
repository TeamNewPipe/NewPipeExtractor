package org.schabi.newpipe.extractor.utils;

import java.util.Locale;

/**
 * This class contains a simple implementation of {@link Locale#forLanguageTag(String)} for Android
 * API levels below 21 (Lollipop). This is needed as core library desugaring does not backport that
 * method as of this writing.
 *
 * Relevant issue: https://issuetracker.google.com/issues/171182330
 */
public final class LocaleCompat {
    private LocaleCompat() {
    }

    // Source: The AndroidX LocaleListCompat class's private forLanguageTagCompat() method.
    // Use Locale.forLanguageTag() on Android API level >= 21 / Java instead.
    public static Locale forLanguageTag(final String str) {
        if (str.contains("-")) {
            final String[] args = str.split("-", -1);
            if (args.length > 2) {
                return new Locale(args[0], args[1], args[2]);
            } else if (args.length > 1) {
                return new Locale(args[0], args[1]);
            } else if (args.length == 1) {
                return new Locale(args[0]);
            }
        } else if (str.contains("_")) {
            final String[] args = str.split("_", -1);
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
