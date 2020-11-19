package org.schabi.newpipe.extractor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        assertEquals("Protocol of URL is not secure", "https", url.getProtocol());
    }

    public static void assertNotEmpty(String stringToCheck) {
        assertNotEmpty(null, stringToCheck);
    }

    public static void assertNotEmpty(@Nullable String message, String stringToCheck) {
        assertNotNull(message, stringToCheck);
        assertFalse(message, stringToCheck.isEmpty());
    }

    public static void assertEmpty(String stringToCheck) {
        assertEmpty(null, stringToCheck);
    }

    public static void assertEmpty(@Nullable String message, String stringToCheck) {
        if (stringToCheck != null) {
            assertTrue(message, stringToCheck.isEmpty());
        }
    }

    public static void assertAtLeast(long expected, long actual) {
        assertTrue(actual + " is not at least " + expected, actual >= expected);
    }

    // this assumes that sorting a and b in-place is not an issue, so it's only intended for tests
    public static void assertEqualsOrderIndependent(List<String> expected, List<String> actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        } else {
            assertNotNull(actual);
        }

        Collections.sort(expected);
        Collections.sort(actual);
        assertEquals(expected, actual);
    }
}
