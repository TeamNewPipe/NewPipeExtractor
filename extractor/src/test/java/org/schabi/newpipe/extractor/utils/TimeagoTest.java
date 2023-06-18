package org.schabi.newpipe.extractor.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.localization.TimeAgoPatternsManager;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeagoTest {
    private static TimeAgoParser parser;
    private static OffsetDateTime now;

    @BeforeAll
    public static void setUp() {
        parser = TimeAgoPatternsManager.getTimeAgoParserFor(Localization.DEFAULT);
        now = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @Test
    void parseTimeago() throws ParsingException {
        assertTimeWithin1s(
                now.minus(1, ChronoUnit.SECONDS),
                parser.parse("1 second ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minus(12, ChronoUnit.SECONDS),
                parser.parse("12 second ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minus(1, ChronoUnit.MINUTES),
                parser.parse("1 minute ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minus(23, ChronoUnit.MINUTES),
                parser.parse("23 minutes ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minus(1, ChronoUnit.HOURS),
                parser.parse("1 hour ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minus(8, ChronoUnit.HOURS),
                parser.parse("8 hours ago").offsetDateTime()
        );
        assertEquals(
                now.minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS),
                parser.parse("1 day ago").offsetDateTime()
        );
        assertEquals(
                now.minus(3, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS),
                parser.parse("3 days ago").offsetDateTime()
        );
        assertEquals(
                now.minus(1, ChronoUnit.WEEKS).truncatedTo(ChronoUnit.HOURS),
                parser.parse("1 week ago").offsetDateTime()
        );
        assertEquals(
                now.minus(3, ChronoUnit.WEEKS).truncatedTo(ChronoUnit.HOURS),
                parser.parse("3 weeks ago").offsetDateTime()
        );
        assertEquals(
                now.minus(1, ChronoUnit.MONTHS).truncatedTo(ChronoUnit.HOURS),
                parser.parse("1 month ago").offsetDateTime()
        );
        assertEquals(
                now.minus(3, ChronoUnit.MONTHS).truncatedTo(ChronoUnit.HOURS),
                parser.parse("3 months ago").offsetDateTime()
        );
        assertEquals(
                now.minus(1, ChronoUnit.YEARS).minusDays(1).truncatedTo(ChronoUnit.HOURS),
                parser.parse("1 year ago").offsetDateTime()
        );
        assertEquals(
                now.minus(3, ChronoUnit.YEARS).minusDays(1).truncatedTo(ChronoUnit.HOURS),
                parser.parse("3 years ago").offsetDateTime()
        );
    }

    @Test
    void parseTimeagoShort() throws ParsingException {
        final TimeAgoParser parser = TimeAgoPatternsManager.getTimeAgoParserFor(Localization.DEFAULT);
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        assertTimeWithin1s(
                now.minus(1, ChronoUnit.SECONDS),
                parser.parse("1 sec ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minus(12, ChronoUnit.SECONDS),
                parser.parse("12 sec ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minus(1, ChronoUnit.MINUTES),
                parser.parse("1 min ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minus(23, ChronoUnit.MINUTES),
                parser.parse("23 min ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minus(1, ChronoUnit.HOURS),
                parser.parse("1 hr ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minus(8, ChronoUnit.HOURS),
                parser.parse("8 hr ago").offsetDateTime()
        );
        assertEquals(
                now.minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS),
                parser.parse("1 day ago").offsetDateTime()
        );
        assertEquals(
                now.minus(3, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS),
                parser.parse("3 days ago").offsetDateTime()
        );
        assertEquals(
                now.minus(1, ChronoUnit.WEEKS).truncatedTo(ChronoUnit.HOURS),
                parser.parse("1 wk ago").offsetDateTime()
        );
        assertEquals(
                now.minus(3, ChronoUnit.WEEKS).truncatedTo(ChronoUnit.HOURS),
                parser.parse("3 wk ago").offsetDateTime()
        );
        assertEquals(
                now.minus(1, ChronoUnit.MONTHS).truncatedTo(ChronoUnit.HOURS),
                parser.parse("1 mo ago").offsetDateTime()
        );
        assertEquals(
                now.minus(3, ChronoUnit.MONTHS).truncatedTo(ChronoUnit.HOURS),
                parser.parse("3 mo ago").offsetDateTime()
        );
        assertEquals(
                now.minus(1, ChronoUnit.YEARS).minusDays(1).truncatedTo(ChronoUnit.HOURS),
                parser.parse("1 yr ago").offsetDateTime()
        );
        assertEquals(
                now.minus(3, ChronoUnit.YEARS).minusDays(1).truncatedTo(ChronoUnit.HOURS),
                parser.parse("3 yr ago").offsetDateTime()
        );
    }

    void assertTimeWithin1s(final OffsetDateTime expected, final OffsetDateTime actual) {
        final long delta = Math.abs(expected.toEpochSecond() - actual.toEpochSecond());
        assertTrue(delta <= 1, String.format("Expected: %s\nActual:   %s", expected, actual));
    }
}
