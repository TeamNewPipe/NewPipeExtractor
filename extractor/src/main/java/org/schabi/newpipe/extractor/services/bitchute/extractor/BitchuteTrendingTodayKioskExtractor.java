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
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.services.bitchute.BitchuteService.BITCHUTE_LINK;

public class BitchuteTrendingTodayKioskExtractor extends KioskExtractor<StreamInfoItem> {

    private Document doc;

    public BitchuteTrendingTodayKioskExtractor(StreamingService streamingService,
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
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        for (Element e : doc.select("#trending-day  div.video-trending-container")) {
            collector.commit(
                    new StreamInfoItemExtractor() {
                        @Override
                        public StreamType getStreamType() throws ParsingException {
                            return StreamType.VIDEO_STREAM;
                        }

                        @Override
                        public boolean isAd() throws ParsingException {
                            return false;
                        }

                        @Override
                        public long getDuration() throws ParsingException {
                            return YoutubeParsingHelper.parseDurationString(
                                    e.select(".video-duration").first().text()
                            );
                        }

                        @Override
                        public long getViewCount() throws ParsingException {
                            return Long.parseLong(e.select(".video-views").first().text());
                        }

                        @Override
                        public String getUploaderName() throws ParsingException {
                            return e.select(".video-trending-channel").first().text();
                        }

                        @Override
                        public String getUploaderUrl() throws ParsingException {
                            return e.select(".video-trending-channel a").first()
                                    .absUrl("href");
                        }

                        @Nullable
                        @Override
                        public String getTextualUploadDate() throws ParsingException {
                            return e.select(".video-trending-details").first().text();
                        }

                        @Nullable
                        @Override
                        public DateWrapper getUploadDate() throws ParsingException {
                            return getTimeAgoParser().parse(getTextualUploadDate());
                        }

                        @Override
                        public String getName() throws ParsingException {
                            return e.select(".video-trending-title").first().text();
                        }

                        @Override
                        public String getUrl() throws ParsingException {
                            return e.select(".video-trending-title a")
                                    .first().absUrl("href");
                        }

                        @Override
                        public String getThumbnailUrl() throws ParsingException {
                            return e.select(".video-trending-image img")
                                    .first().attr("data-src");
                        }
                    }
            );
        }
        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        return "";
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        return null;
    }
}