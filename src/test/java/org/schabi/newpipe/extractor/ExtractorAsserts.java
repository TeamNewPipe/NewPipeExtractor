package org.schabi.newpipe.extractor;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExtractorAsserts {
    public static void assertEmptyErrors(String message, List<Throwable> errors) {
        if(!errors.isEmpty()) {
            for (Throwable throwable : errors) {
                message += "\n  * " + throwable.getMessage();
            }
            throw new AssertionError(message, errors.get(0));
        }
    }

    @Nonnull
    private static URL urlFromString(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new AssertionError("Invalid url: " + url, e);
        }
    }

    public static void assertIsValidUrl(String url) {
        urlFromString(url);
    }

    public static void assertIsSecureUrl(String urlToCheck) {
        URL url = urlFromString(urlToCheck);
        assertEquals("Protocol of URL is not secure", "https", url.getProtocol());
    }
}
