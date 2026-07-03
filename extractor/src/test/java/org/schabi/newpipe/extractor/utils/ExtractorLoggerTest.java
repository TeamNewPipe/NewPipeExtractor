package org.schabi.newpipe.extractor.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * See {@link org.schabi.newpipe.extractor.LoggerExtension} for how global logging is set up for tests
 */
class ExtractorLoggerTest {

    private CapturingLogger logger;

    @BeforeEach
    void setup() {
        logger = new CapturingLogger();
        ExtractorLogger.setLogger(logger);
    }

    @Test
    void replacesSinglePlaceholder() {
        ExtractorLogger.d("T", "Hello {Name}", "Alice");
        assertEquals("Hello Alice", logger.lastDebug);
    }

    @Test
    void replacesMultiplePlaceholdersSequentially() {
        ExtractorLogger.d("T", "A={A} B={B} C={C}", 1, 2, 3);
        assertEquals("A=1 B=2 C=3", logger.lastDebug);
    }

    @Test
    void leavesExtraPlaceholdersWhenNotEnoughArgs() {
        ExtractorLogger.d("T", "First={F} Second={S} Third={T}", "X", "Y");
        assertEquals("First=X Second=Y Third={T}", logger.lastDebug);
    }

    @Test
    void ignoresExtraArgs() {
        ExtractorLogger.d("T", "Only {One}", "X", "Y", "Z");
        assertEquals("Only X", logger.lastDebug);
    }

    @Test
    void noArgsReturnsTemplateUnchanged() {
        ExtractorLogger.d("T", "No placeholders {} here");
        assertEquals("No placeholders {} here", logger.lastDebug);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "Value {Unclosed,Value {Unclosed",
        "{({test_one)}}},}",
        "{{test},{test}",
        "{{test_three}}},{test_three}}",
    })
    void unmatchedBraceLeavesRemainder(final String message, final String expected) {
        ExtractorLogger.d("T", message, "");
        assertEquals(expected, logger.lastDebug);
    }

    @ParameterizedTest
    @CsvSource({
        "{{,{",
        "}},}",
        "{{}},{}",
        "}}{{,}{",
        "hello},hello}",
        "hello{,hello{",
    })
    void doubleBraceEscaping(final String message, final String expected) {
        ExtractorLogger.d("T", message, "unconsumed");
        assertEquals(expected, logger.lastDebug);
    }

    @Test
    void escapedBracesDoNotConsumeArg() {
        ExtractorLogger.d("T", "{{Name}}", "Alice");
        assertEquals("{Name}", logger.lastDebug);
    }

    @Test
    void escapedBraceThenPlaceholder() {
        ExtractorLogger.d("T", "{{literal}} {Value}", "Alice");
        assertEquals("{literal} Alice", logger.lastDebug);
    }

    @ParameterizedTest
    @CsvSource({
        "Value {Unclosed,Value {Unclosed",
        "{({test_one)}}},}",
    })
    void unclosedOrStrayBracesLeaveRemainderUnchanged(final String message, final String expected) {
        ExtractorLogger.d("T", message, "");
        assertEquals(expected, logger.lastDebug);
    }

    @ParameterizedTest
    @CsvSource({
        "{{test},{test}",
        "{{test_three}}},{test_three}}",
    })
    void doubleBraceEscapesLiteralBraces(final String message, final String expected) {
        ExtractorLogger.d("T", message, "");
        assertEquals(expected, logger.lastDebug);
    }

    @Test
    void tripleOpenBraceIsEscapedLiteralThenPlaceholder() {
        ExtractorLogger.d("T", "{{{Name}}}", "Alice");
        assertEquals("{Alice}", logger.lastDebug);
    }

    @Test
    void mixedEscapedAndPlaceholders() {
        ExtractorLogger.d("T", "{A} {{B}} {C}", "1", "2");
        assertEquals("1 {B} 2", logger.lastDebug);
    }

    @Test
    void nullArgFormatsAsNullString() {
        ExtractorLogger.d("T", "value is {V}", (Object) null);
        assertEquals("value is null", logger.lastDebug);
    }

    @Test
    void emptyTemplateWithArgsReturnsEmpty() {
        ExtractorLogger.d("T", "", "X");
        assertEquals("", logger.lastDebug);
    }

    @Test
    void nullTemplatePrintsNull() {
        ExtractorLogger.d("T", (String) null, "X");
        assertNull(logger.lastDebug);
    }

    @Test
    void escapedBracesWithThrowable() {
        RuntimeException ex = new RuntimeException("boom");
        ExtractorLogger.d("T", ex, "{{literal}} {Value}", "Alice");
        assertEquals("{literal} Alice", logger.lastDebug);
        assertSame(ex, logger.lastDebugThrowable);
    }

    @Test
    void debugFormatWithThrowable() {
        RuntimeException ex = new RuntimeException("boom");
        ExtractorLogger.d("T", ex, "Failure {Code} at {Step}", 500, "init");
        assertEquals("Failure 500 at init", logger.lastDebug);
        assertSame(ex, logger.lastDebugThrowable);
    }

    @Test
    void warnFormatWithThrowable() {
        IllegalStateException ex = new IllegalStateException("warned");
        ExtractorLogger.w("T", ex, "Warn {What}", "disk");
        assertEquals("Warn disk", logger.lastWarn);
        assertSame(ex, logger.lastWarnThrowable);
    }

    @Test
    void errorFormatWithThrowable() {
        Exception ex = new Exception("fatal");
        ExtractorLogger.e("T", ex, "Error {Type} code={Code}", "IO", 42);
        assertEquals("Error IO code=42", logger.lastError);
        assertSame(ex, logger.lastErrorThrowable);
    }

    @Test
    void debugFormatWithThrowableNotEnoughArgsLeavesPlaceholder() {
        RuntimeException ex = new RuntimeException("x");
        ExtractorLogger.d("T", ex, "Only one {A} and leftover {B}", "arg1");
        assertEquals("Only one arg1 and leftover {B}", logger.lastDebug);
        assertSame(ex, logger.lastDebugThrowable);
    }

    @Test
    void errorFormatWithThrowableExtraArgsIgnored() {
        Exception ex = new Exception("x");
        ExtractorLogger.e("T", ex, "Val {V}", 10, 20, 30);
        assertEquals("Val 10", logger.lastError);
        assertSame(ex, logger.lastErrorThrowable);
    }

    private static final class CapturingLogger implements Logger {
        String lastDebug;
        Throwable lastDebugThrowable;
        String lastWarn;
        Throwable lastWarnThrowable;
        String lastError;
        Throwable lastErrorThrowable;

        @Override
        public void debug(String tag, String message) {
            lastDebug = message;
            lastDebugThrowable = null;
        }

        @Override
        public void debug(String tag, String message, Throwable throwable) {
            lastDebug = message;
            lastDebugThrowable = throwable;
        }

        @Override
        public void warn(String tag, String message) {
            lastWarn = message;
            lastWarnThrowable = null;
        }

        @Override
        public void warn(String tag, String message, Throwable throwable) {
            lastWarn = message;
            lastWarnThrowable = throwable;
        }

        @Override
        public void error(String tag, String message) {
            lastError = message;
            lastErrorThrowable = null;
        }

        @Override
        public void error(String tag, String message, Throwable t) {
            lastError = message;
            lastErrorThrowable = t;
        }
    }
}