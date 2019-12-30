package org.schabi.newpipe.extractor.localization;

import org.schabi.newpipe.extractor.timeago.PatternsHolder;
import org.schabi.newpipe.extractor.timeago.PatternsManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TimeAgoPatternsManager {
    @Nullable
    private static PatternsHolder getPatternsFor(@Nonnull Localization localization) {
        return PatternsManager.getPatterns(localization.getLanguageCode(), localization.getCountryCode());
    }

    @Nullable
    public static TimeAgoParser getTimeAgoParserFor(@Nonnull Localization localization) {
        final PatternsHolder holder = getPatternsFor(localization);

        if (holder == null) {
            return null;
        }

        return new TimeAgoParser(holder);
    }
}
