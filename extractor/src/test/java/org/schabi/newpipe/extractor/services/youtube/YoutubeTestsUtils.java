package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;

import java.util.Random;

/**
 * Utility class for keeping YouTube tests stateless.
 */
public final class YoutubeTestsUtils {
    private YoutubeTestsUtils() {
        // No impl
    }

    /**
     * Clears static YT states.
     *
     * <p>
     * This method needs to be called to generate all mocks of a test when running different tests
     * at the same time.
     * </p>
     */
    public static void ensureStateless() {
        YoutubeParsingHelper.setConsentAccepted(false);
        YoutubeParsingHelper.resetClientVersionAndKey();
        YoutubeParsingHelper.setNumberGenerator(new Random(1));
        YoutubeStreamExtractor.resetDeobfuscationCode();
    }
}
