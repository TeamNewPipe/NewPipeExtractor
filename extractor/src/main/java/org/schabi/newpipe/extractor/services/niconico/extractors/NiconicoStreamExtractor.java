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
import org.schabi.newpipe.extractor.utils.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NiconicoStreamExtractor extends StreamExtractor {
    private static final String THUMB_INFO_URL = "https://ext.nicovideo.jp/api/getthumbinfo/";
    private static final String UPLOADER_URL = "https://www.nicovideo.jp/user/";
    private Document thumbInfo;
    private Document watch;

    public NiconicoStreamExtractor(StreamingService service, LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public long getViewCount() throws ParsingException {
        return Long.parseLong(thumbInfo.select("view_counter").text());
    }

    @Nonnull
    @Override
    public Description getDescription() throws ParsingException {
        return new Description(thumbInfo.select("description").text(), 3);
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() throws ParsingException {
        return thumbInfo.select("thumbnail_url").text();
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ParsingException {
        return UPLOADER_URL + thumbInfo.select("user_id").text();
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        return thumbInfo.select("user_nickname").text();
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        return thumbInfo.select("user_icon_url").text();
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Nullable
    @Override
    public InfoItemsCollector<? extends InfoItem, ? extends InfoItemExtractor> getRelatedItems() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String info = downloader.get(THUMB_INFO_URL + getLinkHandler().getId()).responseBody();
        thumbInfo = Jsoup.parse(info);
        watch = Jsoup.parse(downloader.get(getLinkHandler().getUrl()).responseBody());
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return thumbInfo.select("title").text();
    }
}
