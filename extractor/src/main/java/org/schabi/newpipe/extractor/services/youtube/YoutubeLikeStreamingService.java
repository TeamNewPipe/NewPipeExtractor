package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.feed.FeedExtractor;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;

import java.util.List;

import javax.annotation.Nonnull;

public abstract class YoutubeLikeStreamingService extends StreamingService {

    /**
     * Creates a new Streaming service.
     * If you Implement one do not set id within your implementation of this extractor, instead
     * set the id when you put the extractor into {@link org.schabi.newpipe.extractor.ServiceList}
     * All other parameters can be set directly from the overriding constructor.
     *
     * @param id           the number of the service to identify him within the NewPipe frontend
     * @param name         the name of the service
     * @param capabilities the type of media this service can handle
     */
    protected YoutubeLikeStreamingService(
            final int id,
            final String name,
            final List<ServiceInfo.MediaCapability> capabilities) {
        super(id, name, capabilities);
    }

    @Override
    public abstract StreamExtractor getStreamExtractor(final LinkHandler linkHandler);

    @Override
    public abstract ChannelExtractor getChannelExtractor(final ListLinkHandler linkHandler);

    @Override
    public abstract PlaylistExtractor getPlaylistExtractor(final ListLinkHandler linkHandler);

    @Nonnull
    @Override
    public abstract FeedExtractor getFeedExtractor(final String url) throws ExtractionException;
}
