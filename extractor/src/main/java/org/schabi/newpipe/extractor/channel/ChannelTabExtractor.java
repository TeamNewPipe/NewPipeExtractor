package org.schabi.newpipe.extractor.channel;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import javax.annotation.Nonnull;

public abstract class ChannelTabExtractor extends ListExtractor<InfoItem> {

    public ChannelTabExtractor(final StreamingService service,
                               final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Nonnull
    public String getTab() {
        return getLinkHandler().getContentFilters().get(0);
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return getTab();
    }
}
