package org.schabi.newpipe.extractor.utils;

import org.junit.Test;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class UtilsTest {
    @Test
    public void testMixedNumberWordToLong() throws ParsingException {
        assertEquals(10, Utils.mixedNumberWordToLong("10"));
        assertEquals(10.5e3, Utils.mixedNumberWordToLong("10.5K"), 0.0);
        assertEquals(10.5e6, Utils.mixedNumberWordToLong("10.5M"), 0.0);
        assertEquals(10.5e6, Utils.mixedNumberWordToLong("10,5M"), 0.0);
        assertEquals(1.5e9, Utils.mixedNumberWordToLong("1,5B"), 0.0);
    }

    @Test
    public void testJoin() {
        assertEquals("some,random,stuff", Utils.join(",", Arrays.asList("some", "random", "stuff")));
    }
}
