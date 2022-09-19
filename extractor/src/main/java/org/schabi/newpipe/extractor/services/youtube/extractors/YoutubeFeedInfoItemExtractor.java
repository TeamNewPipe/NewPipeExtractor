package org.schabi.newpipe.extractor.services.youtube.extractors;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

public class YoutubeFeedInfoItemExtractor implements StreamInfoItemExtractor {
    private final Element entryElement;

    public YoutubeFeedInfoItemExtractor(final Element entryElement) {
        this.entryElement = entryElement;
    }

    @Override
    public StreamType getStreamType() {
        // It is not possible to determine the stream type using the feed endpoint.
        // All entries are considered a video stream.
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() {
        return false;
    }

    @Override
    public long getDuration() {
        // Not available when fetching through the feed endpoint.
        return -1;
    }

    @Override
    public long getViewCount() {
        return Long.parseLong(entryElement.getElementsByTag("media:statistics").first()
                .attr("views"));
    }

    @Override
    public String getUploaderName() {
        return entryElement.select("author > name").first().text();
    }

    @Override
    public String getUploaderUrl() {
        return entryElement.select("author > uri").first().text();
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
    public String getTextualUploadDate() {
        return entryElement.getElementsByTag("published").first().text();
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        try {
            return new DateWrapper(OffsetDateTime.parse(getTextualUploadDate()));
        } catch (final DateTimeParseException e) {
            throw new ParsingException("Could not parse date (\"" + getTextualUploadDate() + "\")",
                    e);
        }
    }

    @Override
    public String getName() {
        return entryElement.getElementsByTag("title").first().text();
    }

    @Override
    public String getUrl() {
        return entryElement.getElementsByTag("link").first().attr("href");
    }

    @Override
    public String getThumbnailUrl() {
        // The hqdefault thumbnail has some black bars at the top and at the bottom, while the
        // mqdefault doesn't, so return the mqdefault one. It should always exist, according to
        // https://stackoverflow.com/a/20542029/9481500.
        return entryElement.getElementsByTag("media:thumbnail").first().attr("url")
                .replace("hqdefault", "mqdefault");
    }
}
