package org.schabi.newpipe.extractor.channel.tabs;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.search.filter.FilterItem;

import javax.annotation.Nonnull;

/**
 * A {@link ListExtractor} of {@link InfoItem}s for tabs of channels.
 */
public abstract class ChannelTabExtractor extends ListExtractor<InfoItem> {

    protected ChannelTabExtractor(@Nonnull final StreamingService service,
                                  @Nonnull final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Nonnull
    public FilterItem getChannelTabType() {
        return getLinkHandler().getContentFilters().get(0);
    }

    @Nonnull
    @Override
    public String getName() {
        return getChannelTabType().getName();
    }
}
