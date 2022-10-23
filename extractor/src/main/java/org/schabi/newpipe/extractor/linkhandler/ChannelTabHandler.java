package org.schabi.newpipe.extractor.linkhandler;

public class ChannelTabHandler extends ListLinkHandler {
    public enum Tab {
        Playlists,
        Livestreams,
        Shorts,
        Channels,
        Albums,
    }

    private final Tab tab;

    public ChannelTabHandler(final ListLinkHandler linkHandler, final Tab tab) {
        super(linkHandler);
        this.tab = tab;
    }

    public Tab getTab() {
        return tab;
    }
}
