package org.schabi.newpipe.extractor;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.schabi.newpipe.extractor.utils.ExtractorLogger;

public class LoggerExtension implements BeforeAllCallback {
    private static boolean set = false;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (set) return;
        set = true;
        ExtractorLogger.setLogger(new ExtractorLogger.ConsoleLogger());
    }
}