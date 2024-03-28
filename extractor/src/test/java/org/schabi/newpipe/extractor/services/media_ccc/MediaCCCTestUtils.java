package org.schabi.newpipe.extractor.services.media_ccc;

import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCParsingHelper;

public final class MediaCCCTestUtils {

    /**
     * Clears static media.ccc.de states.
     * <p>This method needs to be called in every class before running and recording mock tests.</p>
     */
    public static void ensureStateless() {
        MediaCCCParsingHelper.resetCachedLiveStreamInfo();
    }

}
