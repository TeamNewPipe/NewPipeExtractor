package org.schabi.newpipe.extractor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ExtractorAsserts {
    public static void assertEmptyErrors(String message, List<Throwable> errors) {
        if(!errors.isEmpty()) {
            for (Throwable throwable : errors) {
                message += "\n  * " + throwable.getMessage();
            }
            throw new AssertionError(message, errors.get(0));
        }
    }

    public static void assertIsValidUrl(String url) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new AssertionError("Invalid url: " + url, e);
        }
    }
}
