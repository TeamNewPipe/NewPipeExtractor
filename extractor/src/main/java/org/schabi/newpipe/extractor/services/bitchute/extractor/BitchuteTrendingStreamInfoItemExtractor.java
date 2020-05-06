package org.schabi.newpipe.extractor.services.bitchute.extractor;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nullable;

public class BitchuteTrendingStreamInfoItemExtractor implements StreamInfoItemExtractor {

    private final Element element;
    private final TimeAgoParser parser;

    public BitchuteTrendingStreamInfoItemExtractor(TimeAgoParser parser, Element element) {
        this.element = element;
        this.parser = parser;
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() {
        return false;
    }

    @Override
    public long getDuration() throws ParsingException {
        try {
            return YoutubeParsingHelper
                    .parseDurationString(element.select(".video-duration")
                            .first().text());
        } catch (Exception e) {
            throw new ParsingException("Error parsing duration");
        }
    }

    @Override
    public long getViewCount() throws ParsingException {
        try {
            return Utils.mixedNumberWordToLong(element
                    .select(".video-views").first().text());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ParsingException("Error parsing view count");
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        try {
            return element.select(".video-trending-channel").first().text();
        } catch (Exception e) {
            throw new ParsingException("Error parsing uploader name");
        }
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        try {
            return element.select(".video-trending-channel a").first()
                    .absUrl("href");
        } catch (Exception e) {
            throw new ParsingException("Error parsing uploader url");
        }
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        try {
            return element.select(".video-trending-details").first().text();
        } catch (Exception e) {
            throw new ParsingException("Error parsing Textual Upload Date");
        }
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return parser.parse(getTextualUploadDate());
    }

    @Override
    public String getName() throws ParsingException {
        try {
            return element.select(".video-trending-title").first().text();
        } catch (Exception e) {
            throw new ParsingException("Error parsing Stream title");
        }
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            return element.select(".video-trending-title a")
                    .first().absUrl("href");
        } catch (Exception e) {
            throw new ParsingException("Error parsing Stream url");
        }
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        try {
            return element.select(".video-trending-image img")
                    .first().attr("data-src");
        } catch (Exception e) {
            throw new ParsingException("Error parsing thumbnail url");
        }
    }

}
