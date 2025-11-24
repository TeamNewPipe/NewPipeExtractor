package org.schabi.newpipe.extractor.localization;

import org.schabi.newpipe.extractor.timeago.PatternMap;
import org.schabi.newpipe.extractor.timeago.PatternsHolder;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
        return getTimeAgoParserFor(locale, LocalDateTime.now());
    }

    @Nullable
    public static TimeAgoParser getTimeAgoParserFor(@Nonnull final Locale locale,
                                                    @Nonnull final LocalDateTime now) {
        final PatternsHolder holder = getPatternsFor(locale);
        return holder == null ? null : new TimeAgoParser(holder, now);
    }
}
