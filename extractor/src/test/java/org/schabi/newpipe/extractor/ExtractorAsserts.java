package org.schabi.newpipe.extractor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.schabi.newpipe.extractor.utils.Utils;

public class ExtractorAsserts {
    public static void assertEmptyErrors(String message, List<Throwable> errors) {
        if (!errors.isEmpty()) {
            StringBuilder messageBuilder = new StringBuilder(message);
            for (Throwable e : errors) {
                messageBuilder.append("\n  * ").append(e.getMessage());
            }
            messageBuilder.append(" ");
            throw new AssertionError(messageBuilder.toString(), errors.get(0));
        }
    }

    @Nonnull
    private static URL urlFromString(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new AssertionError("Invalid url: " + "\"" + url + "\"", e);
        }
    }

    public static void assertIsValidUrl(String url) {
        urlFromString(url);
    }

    public static void assertIsSecureUrl(String urlToCheck) {
        URL url = urlFromString(urlToCheck);
        assertEquals("https", url.getProtocol(), "Protocol of URL is not secure");
    }

    public static void assertNotEmpty(String stringToCheck) {
        assertNotEmpty(null, stringToCheck);
    }

    public static void assertNotEmpty(@Nullable String message, String stringToCheck) {
        assertNotNull(message, stringToCheck);
        assertFalse(stringToCheck.isEmpty(), message);
    }

    public static void assertEmpty(String stringToCheck) {
        assertEmpty(null, stringToCheck);
    }

    public static void assertEmpty(@Nullable String message, String stringToCheck) {
        if (stringToCheck != null) {
            assertTrue(stringToCheck.isEmpty(), message);
        }
    }

    public static void assertNotBlank(String stringToCheck) {
        assertNotBlank(stringToCheck, null);
    }

    public static void assertNotBlank(String stringToCheck, @Nullable String message) {
        assertFalse(Utils.isBlank(stringToCheck), message);
    }

    public static void assertGreater(final long expected, final long actual) {
        assertGreater(expected, actual, actual + " is not > " + expected);
    }

    public static void assertGreater(
            final long expected,
            final long actual,
            final String message
    ) {
        assertTrue(actual > expected, message);
    }

    public static void assertGreaterOrEqual(final long expected, final long actual) {
        assertGreaterOrEqual(expected, actual, actual + " is not >= " + expected);
    }

    public static void assertGreaterOrEqual(
            final long expected,
            final long actual,
            final String message
    ) {
        assertTrue(actual >= expected, message);
    }

    public static void assertLess(final long expected, final long actual) {
        assertLess(expected, actual, actual + " is not < " + expected);
    }

    public static void assertLess(
            final long expected,
            final long actual,
            final String message
    ) {
        assertTrue(actual < expected, message);
    }

    public static void assertLessOrEqual(final long expected, final long actual) {
        assertLessOrEqual(expected, actual, actual + " is not <= " + expected);
    }

    public static void assertLessOrEqual(
            final long expected,
            final long actual,
            final String message
    ) {
        assertTrue(actual <= expected, message);
    }

    // this assumes that sorting a and b in-place is not an issue, so it's only intended for tests
    public static void assertEqualsOrderIndependent(final List<String> expected,
                                                    final List<String> actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        } else {
            assertNotNull(actual);
        }

        Collections.sort(expected);
        Collections.sort(actual);
        // using new ArrayList<> to make sure the type is the same
        assertEquals(new ArrayList<>(expected), new ArrayList<>(actual));
    }

    public static void assertContains(
            final String shouldBeContained,
            final String container) {
        assertNotNull(shouldBeContained, "shouldBeContained is null");
        assertNotNull(container, "container is null");
        assertTrue(container.contains(shouldBeContained),
                "'" + shouldBeContained + "' should be contained inside '" + container +"'");
    }
}
