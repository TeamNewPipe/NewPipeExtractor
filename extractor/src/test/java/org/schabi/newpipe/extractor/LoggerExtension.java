package org.schabi.newpipe.extractor;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.schabi.newpipe.extractor.utils.ExtractorLogger;

/**
 * JUnit extension class for globally setting up the logger for all extractor tests<br>
 * See <a href="https://www.baeldung.com/junit-5-extensions#1-automatic-extension-registration">here</a> on how this works
 * <br>
 * To disable this, set {@code junit.jupiter.extensions.autodetection.enabled = false}
 * in junit-platform.properties
 */
public class LoggerExtension implements BeforeAllCallback {
    private static boolean set = false;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (set) return;
        set = true;
        ExtractorLogger.setLogger(new ExtractorLogger.ConsoleLogger());
    }
}