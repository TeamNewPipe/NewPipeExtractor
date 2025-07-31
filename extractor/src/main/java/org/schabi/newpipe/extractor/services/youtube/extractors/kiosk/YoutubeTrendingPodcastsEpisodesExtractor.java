package org.schabi.newpipe.extractor.services.youtube.extractors.kiosk;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

public class YoutubeTrendingPodcastsEpisodesExtractor extends YoutubeDesktopBaseKioskExtractor {

    public YoutubeTrendingPodcastsEpisodesExtractor(final StreamingService streamingService,
                                                    final ListLinkHandler linkHandler,
                                                    final String kioskId) {
        super(streamingService, linkHandler, kioskId, "FEpodcasts_destination", "qgcCCAM%3D");
    }
}
