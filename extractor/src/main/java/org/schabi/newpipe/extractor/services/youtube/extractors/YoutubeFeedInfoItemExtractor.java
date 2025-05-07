package org.schabi.newpipe.extractor.services.youtube.extractors;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.Image.ResolutionLevel;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

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

    @Nonnull
    @Override
    public List<Image> getThumbnails() {
        final Element thumbnailElement = entryElement.getElementsByTag("media:thumbnail").first();
        if (thumbnailElement == null) {
            return List.of();
        }

        final String feedThumbnailUrl = thumbnailElement.attr("url");

        // If the thumbnail URL is empty, it means that no thumbnail is available, return an empty
        // list in this case
        if (feedThumbnailUrl.isEmpty()) {
            return List.of();
        }

        // The hqdefault thumbnail has some black bars at the top and at the bottom, while the
        // mqdefault doesn't, so return the mqdefault one. It should always exist, according to
        // https://stackoverflow.com/a/20542029/9481500.
        final String newFeedThumbnailUrl = feedThumbnailUrl.replace("hqdefault", "mqdefault");

        int height;
        int width;

        // If the new thumbnail URL is equal to the feed one, it means that a different image
        // resolution is used on feeds, so use the height and width provided instead of the
        // mqdefault ones
        if (newFeedThumbnailUrl.equals(feedThumbnailUrl)) {
            try {
                height = Integer.parseInt(thumbnailElement.attr("height"));
            } catch (final NumberFormatException e) {
                height = Image.HEIGHT_UNKNOWN;
            }

            try {
                width = Integer.parseInt(thumbnailElement.attr("width"));
            } catch (final NumberFormatException e) {
                width = Image.WIDTH_UNKNOWN;
            }
        } else {
            height = 320;
            width = 180;
        }

        return List.of(
                new Image(newFeedThumbnailUrl, height, width, ResolutionLevel.fromHeight(height)));
    }
}
