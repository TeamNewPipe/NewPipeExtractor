package org.schabi.newpipe.extractor.localization;

import org.schabi.newpipe.extractor.timeago.PatternMap;
import org.schabi.newpipe.extractor.timeago.PatternsHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Locale;

public final class TimeAgoPatternsManager {
    private TimeAgoPatternsManager() {
    }

    @Nullable
    private static PatternsHolder getPatternsFor(@Nonnull final Locale locale) {
        final var holder = PatternMap.getPattern(locale.toLanguageTag());
        if (holder != null) {
            return holder;
        }
        return PatternMap.getPattern(locale.getLanguage());
    }

    @Nullable
    public static TimeAgoParser getTimeAgoParserFor(@Nonnull final Locale locale) {
        final var holder = getPatternsFor(locale);
        return holder == null ? null : new TimeAgoParser(holder);
    }

    @Nullable
    public static TimeAgoParser getTimeAgoParserFor(@Nonnull final Locale locale,
                                                    @Nonnull final OffsetDateTime now) {
        final var holder = getPatternsFor(locale);
        return holder == null ? null : new TimeAgoParser(holder, now);
    }
}
