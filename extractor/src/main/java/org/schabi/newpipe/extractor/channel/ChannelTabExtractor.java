package org.schabi.newpipe.extractor.channel;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabHandler;

import javax.annotation.Nonnull;

public abstract class ChannelTabExtractor extends ListExtractor<InfoItem> {

    public ChannelTabExtractor(final StreamingService service,
                               final ChannelTabHandler linkHandler) {
        super(service, linkHandler);
    }

    @Nonnull
    @Override
    public ChannelTabHandler getLinkHandler() {
        return (ChannelTabHandler) super.getLinkHandler();
    }

    @Nonnull
    public ChannelTabHandler.Tab getTab() {
        return getLinkHandler().getTab();
    }

    @Nonnull
    @Override
    public String getName() {
        return getTab().name();
    }
}
