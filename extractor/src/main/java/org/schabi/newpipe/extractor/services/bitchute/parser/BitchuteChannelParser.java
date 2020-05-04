package org.schabi.newpipe.extractor.services.bitchute.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.NewPipe.getDownloader;

public class BitchuteChannelParser {
    private Document doc;
    private LinkHandler linkHandler;
    private String avatarUrl;
    private String description;
    private String name;
    private String subscriberCount;
    private int offset = 0;

    public BitchuteChannelParser(final LinkHandler linkHandler) throws
            IOException, ExtractionException {
        Response response = getDownloader().get(linkHandler.getUrl(),
                BitchuteParserHelper.getBasicHeader());
        this.linkHandler = linkHandler;
        doc = Jsoup.parse(response.responseBody(), linkHandler.getUrl());
        avatarUrl = doc.select("#page-bar > div > div > div.image-container > a > img")
                .first().text();
        name = doc.select("#channel-title").first().text();
        description = doc.select("#channel-description").first().text();
        subscriberCount = BitchuteParserHelper.getSubscriberCountForChannelID(linkHandler.getId());
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getSubscriberCount() {
        return subscriberCount;
    }

    public InfoItemsPage<StreamInfoItem> getInitialInfoItemsPage(int serviceID) {
        return getInfoItemsPage(serviceID, this.doc);
    }

    public InfoItemsPage<StreamInfoItem> getInfoItemsPageForOffset(int serviceID, String offset)
            throws IOException, ExtractionException {
        return getInfoItemsPage(serviceID, BitchuteParserHelper
                .getExtendDocumentForUrl(linkHandler.getUrl(), offset));
    }

    private InfoItemsPage<StreamInfoItem> getInfoItemsPage(int ServiceID, Document doc) {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(ServiceID);
        Elements videos = doc.select(".channel-videos-container");
        for (Element e : videos) {
            collector.commit(new StreamInfoItemExtractor() {
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
                    return YoutubeParsingHelper.parseDurationString(e.
                            select("span.video-duration").first().text());
                }

                @Override
                public long getViewCount() throws ParsingException {
                    return Long.parseLong(e.select("span.video-views").first().text());
                }

                @Override
                public String getUploaderName() throws ParsingException {
                    return name;
                }

                @Override
                public String getUploaderUrl() throws ParsingException {
                    return linkHandler.getUrl();
                }

                @Nullable
                @Override
                public String getTextualUploadDate() throws ParsingException {
                    return e.select("div.channel-videos-details").first().text();
                }

                @Nullable
                @Override
                public DateWrapper getUploadDate() throws ParsingException {

                    Date date;
                    try {
                        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
                        date = df.parse(getTextualUploadDate());
                    } catch (ParseException e) {
                        throw new ParsingException("Couldn't parse date");
                    }
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    return new DateWrapper(calendar);
                }

                @Override
                public String getName() throws ParsingException {
                    return e.select("div.channel-videos-title").first().text();
                }

                @Override
                public String getUrl() throws ParsingException {
                    return e.select("div.channel-videos-title > a").first()
                            .absUrl("href");
                }

                @Override
                public String getThumbnailUrl() throws ParsingException {
                    return e.select("div.channel-videos-image > img").first()
                            .absUrl("data-src");
                }
            });
        }
        if (videos.size() < 25)
            return new InfoItemsPage<>(collector, null);
        offset += 25;
        return new InfoItemsPage<>(collector, String.valueOf(offset));
    }


}