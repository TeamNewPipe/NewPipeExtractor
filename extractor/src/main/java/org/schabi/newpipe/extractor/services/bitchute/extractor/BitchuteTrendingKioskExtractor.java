package org.schabi.newpipe.extractor.services.bitchute.extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.StreamingService;
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

public class BitchuteTrendingKioskExtractor extends KioskExtractor<StreamInfoItem> {

    private Document doc;

    public BitchuteTrendingKioskExtractor(StreamingService streamingService,
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
    public InfoItemsPage<StreamInfoItem> getInitialPage() {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        String selector;
        switch (getId()){
            case TRENDING_MONTH:
                selector = "#trending-month  div.video-trending-container";
                break;
            case TRENDING_WEEK:
                selector = "#trending-week  div.video-trending-container";
                break;
            case TRENDING_DAY:
            default:
                selector = "#trending-day  div.video-trending-container";
        }
        for (final Element e : doc.select(selector)) {
            collector.commit(new BitchuteTrendingStreamInfoItemExtractor(getTimeAgoParser(), e));
        }
        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public String getNextPageUrl() {
        return "";
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(String pageUrl){
        return null;
    }
}