package org.schabi.newpipe.extractor.linkhandler;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;

/**
 * A {@link ListLinkHandler} which can be used to be returned from {@link
 * org.schabi.newpipe.extractor.channel.ChannelInfo#getTabs() ChannelInfo#getTabs()} when a
 * specific tab's data has already been fetched.
 *
 * <p>
 * This class allows passing a builder for a {@link ChannelTabExtractor} that can hold references
 * to variables.
 * </p>
 *
 * <p>
 * Note: a service that wishes to use this class in one of its {@link
 * org.schabi.newpipe.extractor.channel.ChannelExtractor ChannelExtractor}s must also add the
 * following snippet of code in the service's
 * {@link StreamingService#getChannelTabExtractor(ListLinkHandler)}:
 * <pre>
 * if (linkHandler instanceof ReadyChannelTabListLinkHandler) {
 *     return ((ReadyChannelTabListLinkHandler) linkHandler).getChannelTabExtractor(this);
 * }
 * </pre>
 * </p>
 */
public class ReadyChannelTabListLinkHandler extends ListLinkHandler {

    public interface ChannelTabExtractorBuilder extends Serializable {
        @Nonnull
        ChannelTabExtractor build(@Nonnull StreamingService service,
                                  @Nonnull ListLinkHandler linkHandler);
    }

    private final ChannelTabExtractorBuilder extractorBuilder;

    public ReadyChannelTabListLinkHandler(
            final String url,
            final String channelId,
            @Nonnull final String channelTab,
            @Nonnull final ChannelTabExtractorBuilder extractorBuilder) {
        super(url, url, channelId, List.of(channelTab), "");
        this.extractorBuilder = extractorBuilder;
    }

    @Nonnull
    public ChannelTabExtractor getChannelTabExtractor(@Nonnull final StreamingService service) {
        return extractorBuilder.build(service, new ListLinkHandler(this));
    }
}
