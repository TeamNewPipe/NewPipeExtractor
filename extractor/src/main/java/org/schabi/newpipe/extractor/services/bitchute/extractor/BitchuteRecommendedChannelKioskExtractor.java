package org.schabi.newpipe.extractor.services.bitchute.extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemsCollector;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.io.IOException;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.services.bitchute.BitchuteService.BITCHUTE_LINK;
import static org.schabi.newpipe.extractor.services.bitchute.linkHandler.BitchuteKioskLinkHandlerFactory.TRENDING_DAY;
import static org.schabi.newpipe.extractor.services.bitchute.linkHandler.BitchuteKioskLinkHandlerFactory.TRENDING_MONTH;
import static org.schabi.newpipe.extractor.services.bitchute.linkHandler.BitchuteKioskLinkHandlerFactory.TRENDING_WEEK;

public class BitchuteRecommendedChannelKioskExtractor extends KioskExtractor<ChannelInfoItem> {

    private Document doc;

    public BitchuteRecommendedChannelKioskExtractor(StreamingService streamingService,
                                                    ListLinkHandler linkHandler, String kioskId) {
        super(streamingService, linkHandler, kioskId);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        doc = Jsoup.parse(downloader.get(BITCHUTE_LINK, getExtractorLocalization()).responseBody()
                , BITCHUTE_LINK);
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return getId();
    }

    @Nonnull
    @Override
    public InfoItemsPage<ChannelInfoItem> getInitialPage() {
        ChannelInfoItemsCollector collector = new ChannelInfoItemsCollector(getServiceId());
        String selector = "#channel-list .channel-card";
        for (final Element e : doc.select(selector)) {
            collector.commit(new BitchuteRecommendedChannelInfoItemExtractor(e));
        }
        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public String getNextPageUrl() {
        return "";
    }

    @Override
    public InfoItemsPage<ChannelInfoItem> getPage(String pageUrl) {
        return null;
    }
}