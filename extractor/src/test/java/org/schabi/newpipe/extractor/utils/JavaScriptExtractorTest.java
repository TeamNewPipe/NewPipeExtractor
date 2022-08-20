package org.schabi.newpipe.extractor.utils;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.jsextractor.JavaScriptExtractor;
import org.schabi.newpipe.extractor.utils.jsextractor.Lexer;
import org.schabi.newpipe.extractor.utils.jsextractor.Token;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.FileUtils.resolveTestResource;

public class JavaScriptExtractorTest
{
    @Test
    public void testJsExtractor() throws ParsingException {
        final String src = "Wka=function(d){var x = [/,,/,913,/(,)}/g,\"abcdef}\\\"\",];var y = 10/2/1;return x[1][y];}//some={}random-padding+;";
        final String result = JavaScriptExtractor.matchToClosingBrace(src, "Wka=function");
        assertEquals("(d){var x = [/,,/,913,/(,)}/g,\"abcdef}\\\"\",];var y = 10/2/1;return x[1][y];}", result);
    }

    @Test
    public void testEverythingJs() throws ParsingException, IOException {
        File jsFile = resolveTestResource("es5.js");
        final String js = Files.readString(jsFile.toPath());

        Lexer lexer = new Lexer(js);
        Lexer.Item item = null;

        try {
            while (true) {
                item = lexer.getNextToken();
                if (item.token == Token.EOF) {
                    break;
                }
            }
        } catch (Exception e){
            if (item != null) {
                System.out.println("Issue occured at pos " + item.end + ", after\n" +
                        js.substring(Math.max(0, item.start - 50), item.end));
            }
            throw e;
        }

        assertTrue(lexer.isBalanced());
    }
}
