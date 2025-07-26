package org.schabi.newpipe.extractor.services.youtube.extractors.kiosk;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

public class YoutubeLiveExtractor extends YoutubeDesktopBaseKioskExtractor {

    public YoutubeLiveExtractor(final StreamingService streamingService,
                                final ListLinkHandler linkHandler,
                                final String kioskId) {
        super(streamingService, linkHandler, kioskId, "UC4R8DWoMoI7CAwX8_LjQHig",
                "EgdsaXZldGFikgEDCKEK");
    }
}
