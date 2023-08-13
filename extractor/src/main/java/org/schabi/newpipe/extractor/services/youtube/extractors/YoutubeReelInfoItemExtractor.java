package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getThumbnailsFromInfoItem;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import java.util.List;

/**
 * A {@link StreamInfoItemExtractor} for YouTube's {@code reelItemRenderers}.
 *
 * <p>
 * {@code reelItemRenderers} are returned on YouTube for their short-form contents on almost every
 * place and every major client. They provide a limited amount of information and do not provide
 * the exact view count, any uploader info (name, URL, avatar, verified status) and the upload date.
 * </p>
 */
public class YoutubeReelInfoItemExtractor implements StreamInfoItemExtractor {

    @Nonnull
    private final JsonObject reelInfo;
    @Nullable
    private final TimeAgoParser timeAgoParser;

    public YoutubeReelInfoItemExtractor(@Nonnull final JsonObject reelInfo,
                                        @Nullable final TimeAgoParser timeAgoParser) {
        this.reelInfo = reelInfo;
        this.timeAgoParser = timeAgoParser;
    }

    @Override
    public String getName() throws ParsingException {
        return getTextFromObject(reelInfo.getObject("headline"));
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            final String videoId = reelInfo.getString("videoId");
            return YoutubeStreamLinkHandlerFactory.getInstance().getUrl(videoId);
        } catch (final Exception e) {
            throw new ParsingException("Could not get URL", e);
        }
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        return getThumbnailsFromInfoItem(reelInfo);
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public long getDuration() throws ParsingException {
        // Duration of reelItems is only provided in the accessibility data
        // example: "VIDEO TITLE - 49 seconds - play video"
        // "VIDEO TITLE - 1 minute, 1 second - play video"
        final String accessibilityLabel = reelInfo.getObject("accessibility")
                .getObject("accessibilityData").getString("label");
        if (accessibilityLabel == null || timeAgoParser == null) {
            return 0;
        }

        // This approach may be language dependent
        final String[] labelParts = accessibilityLabel.split(" [\u2013-] ");

        if (labelParts.length > 2) {
            final String textualDuration = labelParts[labelParts.length - 2];
            return timeAgoParser.parseDuration(textualDuration);
        }

        return -1;
    }

    @Override
    public long getViewCount() throws ParsingException {
        final String viewCountText = getTextFromObject(reelInfo.getObject("viewCountText"));
        if (!isNullOrEmpty(viewCountText)) {
            // This approach is language dependent
            if (viewCountText.toLowerCase().contains("no views")) {
                return 0;
            }

            return Utils.mixedNumberWordToLong(viewCountText);
        }

        throw new ParsingException("Could not get short view count");
    }

    @Override
    public boolean isShortFormContent() {
        return true;
    }

    // All the following properties cannot be obtained from reelItemRenderers

    @Override
    public boolean isAd() throws ParsingException {
        return false;
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return null;
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
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
