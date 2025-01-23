package org.schabi.newpipe.extractor.localization;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.localization.TimeAgoParserTest.ParseTimeAgoTestData.greaterThanDay;
import static org.schabi.newpipe.extractor.localization.TimeAgoParserTest.ParseTimeAgoTestData.lessThanDay;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

class TimeAgoParserTest {
    public static Stream<Arguments> parseTimeAgo() {
        return Stream.of(
            lessThanDay(Duration.ofSeconds(1), "1 second", "1 sec"),
            lessThanDay(Duration.ofSeconds(12), "12 second", "12 sec"),
            lessThanDay(Duration.ofMinutes(1), "1 minute", "1 min"),
            lessThanDay(Duration.ofMinutes(23), "23 minutes", "23 min"),
            lessThanDay(Duration.ofHours(1), "1 hour", "1 hr"),
            lessThanDay(Duration.ofHours(8), "8 hour", "8 hr"),
            greaterThanDay(d -> d.minusDays(1), "1 day", "1 day"),
            greaterThanDay(d -> d.minusDays(3), "3 days", "3 day"),
            greaterThanDay(d -> d.minusWeeks(1), "1 week", "1 wk"),
            greaterThanDay(d -> d.minusWeeks(3), "3 weeks", "3 wk"),
            greaterThanDay(d -> d.minusMonths(1), "1 month", "1 mo"),
            greaterThanDay(d -> d.minusMonths(3), "3 months", "3 mo"),
            greaterThanDay(d -> d.minusYears(1).minusDays(1), "1 year", "1 yr"),
            greaterThanDay(d -> d.minusYears(3).minusDays(1), "3 years", "3 yr")
        ).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource
    void parseTimeAgo(final ParseTimeAgoTestData testData) {
        final OffsetDateTime now = OffsetDateTime.of(
            LocalDateTime.of(2020, 1, 1, 1, 1, 1),
            ZoneOffset.UTC);
        final TimeAgoParser parser = Objects.requireNonNull(
            TimeAgoPatternsManager.getTimeAgoParserFor(Localization.DEFAULT, now));

        final OffsetDateTime expected = testData.getExpectedApplyToNow().apply(now);

        assertAll(
            Stream.of(
                    testData.getTextualDateLong(),
                    testData.getTextualDateShort())
                .map(textualDate -> () -> assertEquals(
                    expected,
                    parser.parse(textualDate).offsetDateTime(),
                    "Expected " + expected + " for " + textualDate
                ))
        );
    }

    static class ParseTimeAgoTestData {
        public static final String AGO_SUFFIX = " ago";
        private final Function<OffsetDateTime, OffsetDateTime> expectedApplyToNow;
        private final String textualDateLong;
        private final String textualDateShort;

        ParseTimeAgoTestData(
            final Function<OffsetDateTime, OffsetDateTime> expectedApplyToNow,
            final String textualDateLong,
            final String textualDateShort
        ) {
            this.expectedApplyToNow = expectedApplyToNow;
            this.textualDateLong = textualDateLong;
            this.textualDateShort = textualDateShort;
        }

        public static ParseTimeAgoTestData lessThanDay(
            final Duration duration,
            final String textualDateLong,
            final String textualDateShort
        ) {
            return new ParseTimeAgoTestData(
                d -> d.minus(duration),
                textualDateLong + AGO_SUFFIX,
                textualDateShort + AGO_SUFFIX);
        }

        public static ParseTimeAgoTestData greaterThanDay(
            final Function<OffsetDateTime, OffsetDateTime> expectedApplyToNow,
            final String textualDateLong,
            final String textualDateShort
        ) {
            return new ParseTimeAgoTestData(
                d -> expectedApplyToNow.apply(d).truncatedTo(ChronoUnit.HOURS),
                textualDateLong + AGO_SUFFIX,
                textualDateShort + AGO_SUFFIX);
        }

        public Function<OffsetDateTime, OffsetDateTime> getExpectedApplyToNow() {
            return expectedApplyToNow;
        }

        public String getTextualDateLong() {
            return textualDateLong;
        }

        public String getTextualDateShort() {
            return textualDateShort;
        }
    }
}
