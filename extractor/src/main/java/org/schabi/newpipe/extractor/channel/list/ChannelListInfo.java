package org.schabi.newpipe.extractor.channel.list;

import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.ListInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.utils.ExtractorHelper;

import java.io.IOException;

import javax.annotation.Nonnull;

public class ChannelListInfo extends ListInfo<ChannelInfoItem> {
    public ChannelListInfo(final int serviceId,
                           @Nonnull final ListLinkHandler linkHandler,
                           final String name) {
        super(serviceId, linkHandler, name);
    }

    public static ChannelListInfo getInfo(final String url)
            throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url);
    }

    public static ChannelListInfo getInfo(final StreamingService service, final String url)
            throws IOException, ExtractionException {
        final ChannelListExtractor extractor = service.getChannelListExtractor(url);
        extractor.fetchPage();
        return getInfo(extractor);
    }

    /**
     * Get a {@link ChannelListInfo} instance from the given service and link handler.
     *
     * @param service streaming service
     * @param linkHandler Channel list handler (from {@link ChannelListInfo})
     * @return the extracted {@link ChannelListInfo}
     */
    @Nonnull
    public static ChannelListInfo getInfo(@Nonnull final StreamingService service,
                                         @Nonnull final ListLinkHandler linkHandler)
            throws ExtractionException, IOException {
        final ChannelListExtractor extractor = service.getChannelListExtractor(linkHandler);
        extractor.fetchPage();

        return getInfo(extractor);
    }

    /**
     * Get a {@link ChannelListInfo} instance from a {@link ChannelListExtractor}.
     *
     * @param extractor an extractor where {@code fetchPage()} was already got called on
     * @return the extracted {@link ChannelListInfo}
     */
    @Nonnull
    public static ChannelListInfo getInfo(@Nonnull final ChannelListExtractor extractor)
            throws ParsingException {
        final ChannelListInfo info =
                new ChannelListInfo(extractor.getServiceId(),
                        extractor.getLinkHandler(),
                        extractor.getName());
        try {

            info.setOriginalUrl(extractor.getOriginalUrl());
        } catch (final Exception e) {
            info.addError(e);
        }

        final ListExtractor.InfoItemsPage<ChannelInfoItem> page
                = ExtractorHelper.getItemsPageOrLogError(info, extractor);
        info.setRelatedItems(page.getItems());
        info.setNextPage(page.getNextPage());

        return info;
    }

    public static ListExtractor.InfoItemsPage<ChannelInfoItem> getMoreItems(
            @Nonnull final StreamingService service,
            @Nonnull final ListLinkHandler linkHandler,
            @Nonnull final Page page) throws ExtractionException, IOException {
        return service.getChannelListExtractor(linkHandler).getPage(page);
    }

}
