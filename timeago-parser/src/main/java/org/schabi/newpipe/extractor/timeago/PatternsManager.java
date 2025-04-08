package org.schabi.newpipe.extractor.timeago;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PatternsManager {
    /**
     * Return an holder object containing all the patterns array.
     *
     * @return an object containing the patterns. If not existent, {@code null}.
     */
    @Nullable
    public static PatternsHolder getPatterns(@Nonnull String languageCode, @Nullable String countryCode) {
        final String targetLocalizationClassName = languageCode +
                (countryCode == null || countryCode.isEmpty() ? "" : "_" + countryCode);
        return PatternMap.getPattern(targetLocalizationClassName);
    }
}
