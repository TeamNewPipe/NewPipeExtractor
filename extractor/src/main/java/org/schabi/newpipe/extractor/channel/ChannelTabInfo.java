package org.schabi.newpipe.extractor.channel;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.ListInfo;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.utils.ExtractorHelper;

import java.io.IOException;

public class ChannelTabInfo extends ListInfo<InfoItem> {
    public ChannelTabInfo(int serviceId, String id, String url, String originalUrl, String name, ListLinkHandler listLinkHandler) {
        super(serviceId, id, url, originalUrl, name, listLinkHandler.getContentFilters(), listLinkHandler.getSortFilter());
    }

    public static ListExtractor.InfoItemsPage<InfoItem> getMoreItems(ChannelTabInfo tabInfo, String pageUrl) throws IOException, ExtractionException {
        return tabInfo.getChannelTabExtractor().getPage(pageUrl);
    }

    public static ChannelTabInfo getInfo(ChannelTabExtractor extractor) throws ExtractionException {
        final int serviceId = extractor.getServiceId();
        final String id = extractor.getId();
        final String url = extractor.getUrl();
        final String originalUrl = extractor.getOriginalUrl();
        final String name = extractor.getName();

        final ChannelTabInfo info = new ChannelTabInfo(serviceId, id, url, originalUrl, name, extractor.getLinkHandler());

        info.setChannelTabExtractor(extractor);

        final ListExtractor.InfoItemsPage<InfoItem> itemsPage = ExtractorHelper.getItemsPageOrLogError(info, extractor);
        info.setRelatedItems(itemsPage.getItems());
        info.setNextPageUrl(itemsPage.getNextPageUrl());

        return info;
    }

    private transient ChannelTabExtractor channelTabExtractor;

    public ChannelTabExtractor getChannelTabExtractor() {
        return channelTabExtractor;
    }

    public void setChannelTabExtractor(ChannelTabExtractor channelTabExtractor) {
        this.channelTabExtractor = channelTabExtractor;
    }
}
