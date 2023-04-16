package org.schabi.newpipe.extractor.linkhandler;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;

import java.io.Serializable;
import java.util.Collections;

/**
 * Can be used as a {@link ListLinkHandler} to be returned from {@link
 * org.schabi.newpipe.extractor.channel.ChannelInfo#getTabs()} when a specific tab's data has
 * already been fetched. This class allows passing a builder for a {@link ChannelTabExtractor} that
 * can hold references to variables.
 *
 * Note: a service that wishes to use this class in one of its {@link
 * org.schabi.newpipe.extractor.channel.ChannelExtractor}s must also add the following snippet of
 * code in the service's {@link StreamingService#getChannelTabExtractor(ListLinkHandler)}:
 * <pre>
 * if (linkHandler instanceof ReadyChannelTabListLinkHandler) {
 *     return ((ReadyChannelTabListLinkHandler) linkHandler).getChannelTabExtractor(this);
 * }
 * </pre>
 */
public class ReadyChannelTabListLinkHandler extends ListLinkHandler {

    public interface ChannelTabExtractorBuilder extends Serializable {
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
