package org.schabi.newpipe.extractor.services.bitchute.extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.bitchute.parser.BitchuteParserHelper;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BitchuteStreamExtractor extends StreamExtractor {

    private Document doc;
    private BitchuteParserHelper.VideoCount videoCount;
    private Elements relatedStreamAsElements;

    public BitchuteStreamExtractor(StreamingService service, LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader)
            throws IOException, ExtractionException {
        Response response = downloader.get(
                getUrl(),
                BitchuteParserHelper.getBasicHeader(), getExtractorLocalization());

        doc = Jsoup.parse(response.responseBody(), getUrl());
        videoCount = BitchuteParserHelper.getVideoCountObjectForStreamID(getId());
        relatedStreamAsElements = doc.select(".video-card");

    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        try {
            return doc.select("#video-title").first().text();
        } catch (Exception e) {
            throw new ParsingException("Error parsing stream name");
        }
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        try {
            return doc.select(".video-publish-date").first().text();
        } catch (Exception e) {
            throw new ParsingException("Error parsing textual upload date");
        }
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        try {
            int in = getTextualUploadDate().indexOf("on");
            in += 2;
            Date date;
            try {
                SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
                date = df.parse(getTextualUploadDate().substring(in));
            } catch (ParseException e) {
                throw new ParsingException("Couldn't parse Date");
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return new DateWrapper(calendar);
        } catch (Exception e) {
            throw new ParsingException("Error parsing upload date");
        }
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() throws ParsingException {
        try {
            return doc.select("#player").first().attr("poster");
        } catch (Exception e) {
            throw new ParsingException("Error parsing thumbnail url");
        }
    }

    @Nonnull
    @Override
    public Description getDescription() throws ParsingException {
        try {
            return new Description(doc.select("#video-description").first().html(),
                    Description.HTML);
        } catch (Exception e) {
            throw new ParsingException("Error parsing description");
        }
    }

    @Override
    public int getAgeLimit() {
        return 16;
    }

    @Override
    public long getLength() {
        return 0;
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

    @Override
    public long getViewCount() {
        return videoCount.getViewCount();
    }

    @Override
    public long getLikeCount() {
        return videoCount.getLikeCount();
    }

    @Override
    public long getDislikeCount() {
        return videoCount.getDislikeCount();
    }

    @Override
    public StreamInfoItem getNextStream() throws ExtractionException {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        collector.commit(new BitchuteStreamRelatedInfoItemExtractor(
                getTimeAgoParser(), relatedStreamAsElements.get(0),
                getUploaderName(), getUploaderUrl()));
        return collector.getItems().get(0);
    }

    @Override
    public StreamInfoItemsCollector getRelatedStreams() throws ExtractionException {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        for (int i = 1; i < relatedStreamAsElements.size(); i++) {
            collector.commit(new BitchuteStreamRelatedInfoItemExtractor(
                    getTimeAgoParser(), relatedStreamAsElements.get(i),
                    getUploaderName(), getUploaderUrl()
            ));
        }
        return collector;
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ParsingException {
        try {
            return doc.select("#video-watch  p.name a").first().absUrl("href");
        } catch (Exception e) {
            throw new ParsingException("Error parsing uploader url");
        }
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        try {
            return doc.select("#video-watch  p.name").first().text();
        } catch (Exception e) {
            throw new ParsingException("Error parsing upload name");
        }
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        try {
            return doc.select("#video-watch div.image-container > a > img")
                    .first().attr("data-src");
        } catch (Exception e) {
            throw new ParsingException("Error parsing upload avatar url");
        }
    }

    @Nonnull
    @Override
    public String getDashMpdUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getHlsUrl() {
        return "";
    }

    @Override
    public List<AudioStream> getAudioStreams() {
        return null;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws ExtractionException {
        try {
            return Collections.singletonList(new VideoStream(doc.select("#player source")
                    .first().attr("src"), MediaFormat.M4A, "480p"));
        } catch (Exception e) {
            throw new ParsingException("Error parsing video stream");
        }
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() {
        return null;
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitlesDefault() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitles(MediaFormat format) {
        return Collections.emptyList();
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Nonnull
    @Override
    public String getHost() {
        return "";
    }

    @Nonnull
    @Override
    public String getPrivacy() {
        return "";
    }

    @Nonnull
    @Override
    public String getCategory() throws ParsingException {
        try {
            return doc.select("#video-description + table tbody  td:nth-child(2) > a").first().text();
        } catch (Exception e) {
            throw new ParsingException("Error parsing category");
        }
    }

    @Nonnull
    @Override
    public String getLicence() {
        return "";
    }

    @Nullable
    @Override
    public Locale getLanguageInfo() {
        return null;
    }

    @Nonnull
    @Override
    public List<String> getTags() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public String getSupportInfo() {
        return "https://www.bitchute.com/help-us-grow/";
    }
}
