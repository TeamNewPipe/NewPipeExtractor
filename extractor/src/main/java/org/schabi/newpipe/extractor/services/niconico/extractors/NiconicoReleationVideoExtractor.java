package org.schabi.newpipe.extractor.services.niconico.extractors;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import javax.annotation.Nullable;

public class NiconicoReleationVideoExtractor implements StreamInfoItemExtractor {
    private final Element video;

    public NiconicoReleationVideoExtractor(final Element v) {
        video = v;
    }

    @Override
    public String getName() throws ParsingException {
        return video.select("title").text();
    }

    @Override
    public String getUrl() throws ParsingException {
        return video.select("url").text();
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return video.select("thumbnail").text();
    }

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
        return Long.parseLong(video.select("length").text());
    }

    @Override
    public long getViewCount() throws ParsingException {
        return Long.parseLong(video.select("view").text());
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return "";
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return "";
    }

    @Nullable
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        return null;
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
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
}
