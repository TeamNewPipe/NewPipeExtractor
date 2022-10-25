package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.linkhandler.ChannelTabHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import javax.annotation.Nullable;

public class YouTubeChannelTabHandler extends ChannelTabHandler {

    /**
     * Since YouTube is currently A/B testing a new tab layout,
     * we need to store the visitor data cookie when fetching a channel and pass it to
     * YouTube when requesting channel tabs. Otherwise YouTube may not enable the A/B test
     * on subsequent requests and return empty tabs.
     * <p>
     * This may be removed when the new layout is made permanent.
     */
    @Nullable
    private final String visitorData;

    public YouTubeChannelTabHandler(final ListLinkHandler linkHandler, final Tab tab,
                                    @Nullable final String visitorData) {
        super(linkHandler, tab);
        this.visitorData = visitorData;
    }

    @Nullable
    public String getVisitorData() {
        return visitorData;
    }
}
