package org.schabi.newpipe.extractor.services.bitchute.extractor;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.bitchute.parser.BitchuteChannelParser;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;

import javax.annotation.Nonnull;

public class BitchuteChannelExtractor extends ChannelExtractor {
    private BitchuteChannelParser parser;

    public BitchuteChannelExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader)
            throws IOException, ExtractionException {
        parser = new BitchuteChannelParser(getLinkHandler());
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return parser.getName();
    }

    @Override
    public String getAvatarUrl() throws ParsingException {
        return parser.getAvatarUrl();
    }

    @Override
    public String getDescription() throws ParsingException {
        return parser.getDescription();
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        return Long.parseLong(parser.getSubscriberCount());
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        return parser.getInitialInfoItemsPage(getServiceId());
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(String pageUrl)
            throws IOException, ExtractionException {
        return parser.getInfoItemsPageForOffset(getServiceId(), pageUrl);
    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        return "25";
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        return null;
    }

    @Override
    public String getFeedUrl() throws ParsingException {
        return null;
    }
}
