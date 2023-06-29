package org.schabi.newpipe.extractor.channel.tabs;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.ListInfo;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.utils.ExtractorHelper;

import javax.annotation.Nonnull;
import java.io.IOException;

public class ChannelTabInfo extends ListInfo<InfoItem> {

    public ChannelTabInfo(final int serviceId,
                          @Nonnull final ListLinkHandler linkHandler) {
        super(serviceId, linkHandler, linkHandler.getContentFilters().get(0));
    }

    /**
     * Get a {@link ChannelTabInfo} instance from the given service and tab handler.
     *
     * @param service streaming service
     * @param linkHandler Channel tab handler (from {@link ChannelInfo})
     * @return the extracted {@link ChannelTabInfo}
     */
    @Nonnull
    public static ChannelTabInfo getInfo(@Nonnull final StreamingService service,
                                         @Nonnull final ListLinkHandler linkHandler)
            throws ExtractionException, IOException {
        final ChannelTabExtractor extractor = service.getChannelTabExtractor(linkHandler);
        extractor.fetchPage();
        return getInfo(extractor);
    }

    /**
     * Get a {@link ChannelTabInfo} instance from a {@link ChannelTabExtractor}.
     *
     * @param extractor an extractor where {@code fetchPage()} was already got called on
     * @return the extracted {@link ChannelTabInfo}
     */
    @Nonnull
    public static ChannelTabInfo getInfo(@Nonnull final ChannelTabExtractor extractor) {
        final ChannelTabInfo info =
                new ChannelTabInfo(extractor.getServiceId(), extractor.getLinkHandler());

        try {
            info.setOriginalUrl(extractor.getOriginalUrl());
        } catch (final Exception e) {
            info.addError(e);
        }

        final ListExtractor.InfoItemsPage<InfoItem> page
                = ExtractorHelper.getItemsPageOrLogError(info, extractor);
        info.setRelatedItems(page.getItems());
        info.setNextPage(page.getNextPage());

        return info;
    }

    public static ListExtractor.InfoItemsPage<InfoItem> getMoreItems(
            @Nonnull final StreamingService service,
            @Nonnull final ListLinkHandler linkHandler,
            @Nonnull final Page page) throws ExtractionException, IOException {
        return service.getChannelTabExtractor(linkHandler).getPage(page);
    }
}
