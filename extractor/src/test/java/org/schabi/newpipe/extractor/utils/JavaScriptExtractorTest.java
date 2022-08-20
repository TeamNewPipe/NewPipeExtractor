package org.schabi.newpipe.extractor.utils;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.jsextractor.JavaScriptExtractor;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaScriptExtractorTest
{
    @Test
    public void testJsExtractor() throws ParsingException {
        final String src = "Wka=function(d){var x = [/,,/,913,/(,)}/g,\"abcdef}\\\"\",];var y = 10/2/1;return x[1][y];}//some={}random-padding+;";
        final String result = JavaScriptExtractor.matchToClosingBrace(src, "Wka=function");
        assertEquals("(d){var x = [/,,/,913,/(,)}/g,\"abcdef}\\\"\",];var y = 10/2/1;return x[1][y];}", result);
    }
}
