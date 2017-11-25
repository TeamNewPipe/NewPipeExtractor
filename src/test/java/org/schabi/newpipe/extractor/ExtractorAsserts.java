package org.schabi.newpipe.extractor;

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
}
