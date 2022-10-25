package org.schabi.newpipe.extractor.linkhandler;

import javax.annotation.Nullable;

public class ChannelTabHandler extends ListLinkHandler {
    public enum Tab {
        Playlists,
        Livestreams,
        Shorts,
        Channels,
        Albums,
    }

    private final Tab tab;

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

    public ChannelTabHandler(final ListLinkHandler linkHandler, final Tab tab,
                             @Nullable final String visitorData) {
        super(linkHandler);
        this.tab = tab;
        this.visitorData = visitorData;
    }

    public ChannelTabHandler(final ListLinkHandler linkHandler, final Tab tab) {
        super(linkHandler);
        this.tab = tab;
        this.visitorData = null;
    }

    public Tab getTab() {
        return tab;
    }

    @Nullable
    public String getVisitorData() {
        return visitorData;
    }
}
