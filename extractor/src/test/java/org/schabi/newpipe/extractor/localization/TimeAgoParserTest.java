package org.schabi.newpipe.extractor.localization;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeAgoParserTest {
    private static TimeAgoParser timeAgoParser;

    @BeforeAll
    static void setUp() {
        timeAgoParser = TimeAgoPatternsManager.getTimeAgoParserFor(Localization.DEFAULT);
    }

    @Test
    void testGetDuration() throws ParsingException {
        assertEquals(1, timeAgoParser.parseDuration("one second"));
        assertEquals(1, timeAgoParser.parseDuration("second"));
        assertEquals(49, timeAgoParser.parseDuration("49 seconds"));
        assertEquals(61, timeAgoParser.parseDuration("1 minute, 1 second"));
    }

    @Test
    void testGetDurationError() {
        assertThrows(ParsingException.class, () -> timeAgoParser.parseDuration("abcd"));
        assertThrows(ParsingException.class, () -> timeAgoParser.parseDuration("12 abcd"));
    }
}