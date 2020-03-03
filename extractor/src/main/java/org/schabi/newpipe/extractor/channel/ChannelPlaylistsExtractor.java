package org.schabi.newpipe.extractor.channel;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

public abstract class ChannelPlaylistsExtractor extends ChannelTabExtractor<StreamInfoItem> {
    public ChannelPlaylistsExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }
}
