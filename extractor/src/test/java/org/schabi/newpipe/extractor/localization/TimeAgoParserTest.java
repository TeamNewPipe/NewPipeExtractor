package org.schabi.newpipe.extractor.localization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.time.Duration;

class TimeAgoParserTest {
    private static TimeAgoParser timeAgoParser;

    @BeforeAll
    static void setUp() {
        timeAgoParser = TimeAgoPatternsManager.getTimeAgoParserFor(Localization.DEFAULT);
    }

    @Test
    void testGetDuration() throws ParsingException {
        assertEquals(Duration.ofSeconds(1), timeAgoParser.parseDuration("one second"));
        assertEquals(Duration.ofSeconds(1), timeAgoParser.parseDuration("second"));
        assertEquals(Duration.ofSeconds(49), timeAgoParser.parseDuration("49 seconds"));
        assertEquals(Duration.ofSeconds(61), timeAgoParser.parseDuration("1 minute, 1 second"));
    }

    @Test
    void testGetDurationError() {
        assertThrows(ParsingException.class, () -> timeAgoParser.parseDuration("abcd"));
        assertThrows(ParsingException.class, () -> timeAgoParser.parseDuration("12 abcd"));
    }
}