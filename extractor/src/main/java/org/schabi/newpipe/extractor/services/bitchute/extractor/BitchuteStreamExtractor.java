package org.schabi.newpipe.extractor.services.bitchute.extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

    Document doc;
    BitchuteParserHelper.VideoCount videoCount;

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

    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return doc.select("#video-title").first().text();
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        return doc.select(".video-publish-date").first().text();
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
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
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() throws ParsingException {
        return doc.select("#player").first().attr("poster");
    }

    @Nonnull
    @Override
    public Description getDescription() throws ParsingException {
        return new Description(doc.select("#video-description").first().html(),
                Description.HTML);
    }

    @Override
    public int getAgeLimit() throws ParsingException {
        //TODO
        return 16;
    }

    @Override
    public long getLength() throws ParsingException {
        return 0;
    }

    @Override
    public long getTimeStamp() throws ParsingException {
        return 0;
    }

    @Override
    public long getViewCount() throws ParsingException {
        return videoCount.getViewCount();
    }

    @Override
    public long getLikeCount() throws ParsingException {
        return videoCount.getLikeCount();
    }

    @Override
    public long getDislikeCount() throws ParsingException {
        return videoCount.getDislikeCount();
    }

    @Override
    public StreamInfoItem getNextStream() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public StreamInfoItemsCollector getRelatedStreams() throws IOException, ExtractionException {
        return null;
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ParsingException {
        return doc.select("#video-watch  p.name a").first().absUrl("href");
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        return doc.select("#video-watch  p.name").first().text();
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        return doc.select("#video-watch div.image-container > a > img")
                .first().attr("data-src");
    }

    @Nonnull
    @Override
    public String getDashMpdUrl() throws ParsingException {
        return "";
    }

    @Nonnull
    @Override
    public String getHlsUrl() throws ParsingException {
        return "";
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        return Collections.singletonList(new VideoStream(doc.select("#player source")
                .first().attr("src"), MediaFormat.M4A, "480p"));
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException {
        return null;
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitlesDefault() throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitles(MediaFormat format) throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Nonnull
    @Override
    public String getHost() throws ParsingException {
        return "";
    }

    @Nonnull
    @Override
    public String getPrivacy() throws ParsingException {
        return "";
    }

    @Nonnull
    @Override
    public String getCategory() throws ParsingException {
        return doc.select("#video-description + table tbody  td:nth-child(2) > a").first().text();
    }

    @Nonnull
    @Override
    public String getLicence() throws ParsingException {
        return "";
    }

    @Nullable
    @Override
    public Locale getLanguageInfo() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public List<String> getTags() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public String getSupportInfo() throws ParsingException {
        //TODO
        return "";
    }
}
