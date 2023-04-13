package org.schabi.newpipe.extractor.linkhandler;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;

import java.util.Collections;
import java.util.List;

public class ReadyChannelTabListLinkHandler extends ListLinkHandler {

    public interface ChannelTabExtractorBuilder {
        ChannelTabExtractor build(StreamingService service, ListLinkHandler linkHandler);
    }

    private final ChannelTabExtractorBuilder extractorBuilder;

    public ReadyChannelTabListLinkHandler(final String url,
                                          final String channelId,
                                          final String channelTab,
                                          final ChannelTabExtractorBuilder extractorBuilder) {
        super(url, url, channelId, Collections.singletonList(channelTab), "");
        this.extractorBuilder = extractorBuilder;
    }

    public ChannelTabExtractor getChannelTabExtractor(final StreamingService service) {
        return extractorBuilder.build(service, new ListLinkHandler(this));
    }
}
