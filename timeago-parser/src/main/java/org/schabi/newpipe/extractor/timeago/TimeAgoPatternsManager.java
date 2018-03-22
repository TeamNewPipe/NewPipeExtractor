package org.schabi.newpipe.extractor.timeago;

public class TimeAgoPatternsManager {
    public static final String RESOURCE_BUNDLE_ARRAY_SEPARATOR = "»»";
    // TODO: Uncomment this
/*
    /**
     * Get an array of patterns from the resources bundle according to the given {@link Locale}.
     * <p>
     * It already handles the string/array separator from the resources, as the normal resource
     * bundle don't really support an array in the properties file.
     * </p>
     *
     * @param timeUnit which time unit to get the patterns array
     * @param locale   locale used to get the localized patterns
     * @return an array of phrases localized according to the given locale.
     *//*
    public static String[] getPatternsArray(TimeUnit timeUnit, Locale locale) {
        return ResourceBundleUTF8.getBundle("i18n.time_units", locale).getString(timeUnit.name().toLowerCase())
                .split(RESOURCE_BUNDLE_ARRAY_SEPARATOR);
    }*/

    // public static Map<TimeUnit, String[]> getAllPatternsFor(Locale locale)
}
