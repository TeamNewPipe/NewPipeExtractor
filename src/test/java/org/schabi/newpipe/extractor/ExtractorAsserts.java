package org.schabi.newpipe.extractor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

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
}
