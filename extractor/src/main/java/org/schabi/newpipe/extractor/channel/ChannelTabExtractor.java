package org.schabi.newpipe.extractor.channel;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

public abstract class ChannelTabExtractor extends ListExtractor<InfoItem> {
    public ChannelTabExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }
}
