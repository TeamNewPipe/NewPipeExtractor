package org.schabi.newpipe.extractor.utils;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.jsextractor.JavaScriptExtractor;
import org.schabi.newpipe.extractor.utils.jsextractor.Lexer;
import org.schabi.newpipe.extractor.utils.jsextractor.Token;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.FileUtils.resolveTestResource;

public class JavaScriptExtractorTest
{
    @Test
    void testJsExtractor() throws ParsingException {
        final String src = "Wka=function(d){var x = [/,,/,913,/(,)}/g,\"abcdef}\\\"\",];var y = 10/2/1;return x[1][y];}//some={}random-padding+;";
        final String result = JavaScriptExtractor.matchToClosingBrace(src, "Wka=function");
        assertEquals("(d){var x = [/,,/,913,/(,)}/g,\"abcdef}\\\"\",];var y = 10/2/1;return x[1][y];}", result);
    }

    @Test
    void testEverythingJs() throws ParsingException, IOException {
        final File jsFile = resolveTestResource("es5.js");
        final StringBuilder contentBuilder = new StringBuilder();
        Files.lines(jsFile.toPath()).forEach(line -> contentBuilder.append(line).append("\n"));

        final String js = contentBuilder.toString();

        final Lexer lexer = new Lexer(js);
        Lexer.ParsedToken parsedToken = null;

        try {
            while (true) {
                parsedToken = lexer.getNextToken();
                if (parsedToken.token == Token.EOF) {
                    break;
                }
            }
        } catch (final Exception e){
            if (parsedToken != null) {
                throw new ParsingException("Issue occured at pos " + parsedToken.end + ", after\n" +
                        js.substring(Math.max(0, parsedToken.start - 50), parsedToken.end), e);
            }
            throw e;
        }

        assertTrue(lexer.isBalanced());
    }
}
