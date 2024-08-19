package org.schabi.newpipe.extractor.localization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class TimeAgoParserTest {
    private static TimeAgoParser parser;
    private static OffsetDateTime now;

    @BeforeAll
    public static void setUp() {
        parser = TimeAgoPatternsManager.getTimeAgoParserFor(Localization.DEFAULT);
        now = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @Test
    void parseTimeAgo() throws ParsingException {
        assertTimeWithin1s(
                now.minusSeconds(1),
                parser.parse("1 second ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minusSeconds(12),
                parser.parse("12 second ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minusMinutes(1),
                parser.parse("1 minute ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minusMinutes(23),
                parser.parse("23 minutes ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minusHours(1),
                parser.parse("1 hour ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minusHours(8),
                parser.parse("8 hours ago").offsetDateTime()
        );
        assertEquals(
                now.minusDays(1).truncatedTo(ChronoUnit.HOURS),
                parser.parse("1 day ago").offsetDateTime()
        );
        assertEquals(
                now.minusDays(3).truncatedTo(ChronoUnit.HOURS),
                parser.parse("3 days ago").offsetDateTime()
        );
        assertEquals(
                now.minusWeeks(1).truncatedTo(ChronoUnit.HOURS),
                parser.parse("1 week ago").offsetDateTime()
        );
        assertEquals(
                now.minusWeeks(3).truncatedTo(ChronoUnit.HOURS),
                parser.parse("3 weeks ago").offsetDateTime()
        );
        assertEquals(
                now.minusMonths(1).truncatedTo(ChronoUnit.HOURS),
                parser.parse("1 month ago").offsetDateTime()
        );
        assertEquals(
                now.minusMonths(3).truncatedTo(ChronoUnit.HOURS),
                parser.parse("3 months ago").offsetDateTime()
        );
        assertEquals(
                now.minusYears(1).minusDays(1).truncatedTo(ChronoUnit.HOURS),
                parser.parse("1 year ago").offsetDateTime()
        );
        assertEquals(
                now.minusYears(3).minusDays(1).truncatedTo(ChronoUnit.HOURS),
                parser.parse("3 years ago").offsetDateTime()
        );
    }

    @Test
    void parseTimeAgoShort() throws ParsingException {
        final TimeAgoParser parser = TimeAgoPatternsManager.getTimeAgoParserFor(Localization.DEFAULT);
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        assertTimeWithin1s(
                now.minusSeconds(1),
                parser.parse("1 sec ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minusSeconds(12),
                parser.parse("12 sec ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minusMinutes(1),
                parser.parse("1 min ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minusMinutes(23),
                parser.parse("23 min ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minusHours(1),
                parser.parse("1 hr ago").offsetDateTime()
        );
        assertTimeWithin1s(
                now.minusHours(8),
                parser.parse("8 hr ago").offsetDateTime()
        );
        assertEquals(
                now.minusDays(1).truncatedTo(ChronoUnit.HOURS),
                parser.parse("1 day ago").offsetDateTime()
        );
        assertEquals(
                now.minusDays(3).truncatedTo(ChronoUnit.HOURS),
                parser.parse("3 days ago").offsetDateTime()
        );
        assertEquals(
                now.minusWeeks(1).truncatedTo(ChronoUnit.HOURS),
                parser.parse("1 wk ago").offsetDateTime()
        );
        assertEquals(
                now.minusWeeks(3).truncatedTo(ChronoUnit.HOURS),
                parser.parse("3 wk ago").offsetDateTime()
        );
        assertEquals(
                now.minusMonths(1).truncatedTo(ChronoUnit.HOURS),
                parser.parse("1 mo ago").offsetDateTime()
        );
        assertEquals(
                now.minusMonths(3).truncatedTo(ChronoUnit.HOURS),
                parser.parse("3 mo ago").offsetDateTime()
        );
        assertEquals(
                now.minusYears(1).minusDays(1).truncatedTo(ChronoUnit.HOURS),
                parser.parse("1 yr ago").offsetDateTime()
        );
        assertEquals(
                now.minusYears(3).minusDays(1).truncatedTo(ChronoUnit.HOURS),
                parser.parse("3 yr ago").offsetDateTime()
        );
    }

    void assertTimeWithin1s(final OffsetDateTime expected, final OffsetDateTime actual) {
        final long delta = Math.abs(expected.toEpochSecond() - actual.toEpochSecond());
        assertTrue(delta <= 1, String.format("Expected: %s\nActual:   %s", expected, actual));
    }
}
