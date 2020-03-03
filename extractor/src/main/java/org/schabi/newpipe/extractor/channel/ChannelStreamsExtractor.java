package org.schabi.newpipe.extractor.channel;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

public abstract class ChannelStreamsExtractor extends ChannelTabExtractor<StreamInfoItem> {
    public ChannelStreamsExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }
}
