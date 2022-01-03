package org.schabi.newpipe.extractor.services.niconico.extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamSegment;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NiconicoStreamExtractor extends StreamExtractor {

    private Document document;

    public NiconicoStreamExtractor(StreamingService service, LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        return null;
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public Description getDescription() throws ParsingException {
        return null;
    }

    @Override
    public int getAgeLimit() throws ParsingException {
        return 0;
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
        return 0;
    }

    @Override
    public long getLikeCount() throws ParsingException {
        return 0;
    }

    @Override
    public long getDislikeCount() throws ParsingException {
        return 0;
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        return null;
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public String getSubChannelUrl() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public String getSubChannelName() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public String getSubChannelAvatarUrl() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public String getDashMpdUrl() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public String getHlsUrl() throws ParsingException {
        assertPageFetched();

    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException {
        return null;
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitlesDefault() throws IOException, ExtractionException {
        return null;
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitles(MediaFormat format) throws IOException, ExtractionException {
        return null;
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return null;
    }

    @Nullable
    @Override
    public InfoItemsCollector<? extends InfoItem, ? extends InfoItemExtractor> getRelatedItems() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Nonnull
    @Override
    public String getHost() throws ParsingException {
        return null;
    }

    @Override
    public Privacy getPrivacy() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public String getCategory() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public String getLicence() throws ParsingException {
        return null;
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
        return null;
    }

    @Nonnull
    @Override
    public List<StreamSegment> getStreamSegments() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public List<MetaInfo> getMetaInfo() throws ParsingException {
        return null;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String html = downloader.get(getLinkHandler().getUrl()).responseBody();
        document = Jsoup.parse(html);
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return null;
    }
}
