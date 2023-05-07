package org.schabi.newpipe.extractor.localization;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TimeAgoParserTest {
    private static TimeAgoParser timeAgoParser;

    @BeforeAll
    static void setUp() {
        timeAgoParser = TimeAgoPatternsManager.getTimeAgoParserFor(Localization.DEFAULT);
    }

    @Test
    void testGetDuration() throws ParsingException {
        assertEquals(timeAgoParser.parseDuration("one second"), 1);
        assertEquals(timeAgoParser.parseDuration("second"), 1);
        assertEquals(timeAgoParser.parseDuration("49 seconds"), 49);
        assertEquals(timeAgoParser.parseDuration("1 minute, 1 second"), 61);
    }

    @Test
    void testGetDurationError() {
        assertThrows(ParsingException.class, () -> timeAgoParser.parseDuration("abcd"));
        assertThrows(ParsingException.class, () -> timeAgoParser.parseDuration("12 abcd"));
    }
}
