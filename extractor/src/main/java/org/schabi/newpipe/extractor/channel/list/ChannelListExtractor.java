package org.schabi.newpipe.extractor.channel.list;

import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

public abstract class ChannelListExtractor extends ListExtractor<ChannelInfoItem> {
    public ChannelListExtractor(final StreamingService service,
                                final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }
}
