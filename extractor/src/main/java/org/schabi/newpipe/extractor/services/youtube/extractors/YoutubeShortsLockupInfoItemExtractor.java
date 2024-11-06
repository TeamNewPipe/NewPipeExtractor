package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/**
 * A {@link StreamInfoItemExtractor} for YouTube's {@code shortsLockupViewModel}s.
 *
 * <p>
 * {@code shortsLockupViewModel}s are returned on YouTube for their short-form contents on almost
 * every place and every major client. They provide a limited amount of information and do not
 * provide the exact view count, any uploader info (name, URL, avatar, verified status) and the
 * upload date.
 * </p>
 *
 * <p>
 * At the time this documentation has been written, this data UI type is not fully used (rolled
 * out), so {@code reelItemRenderer}s are also returned. See {@link YoutubeReelInfoItemExtractor}
 * for an extractor for this UI data type.
 * </p>
 */
public class YoutubeShortsLockupInfoItemExtractor implements StreamInfoItemExtractor {

    @Nonnull
    private final JsonObject shortsLockupViewModel;

    public YoutubeShortsLockupInfoItemExtractor(@Nonnull final JsonObject shortsLockupViewModel) {
        this.shortsLockupViewModel = shortsLockupViewModel;
    }

    @Override
    public String getName() throws ParsingException {
        return shortsLockupViewModel.getObject("overlayMetadata")
                .getObject("primaryText")
                .getString("content");
    }

    @Override
    public String getUrl() throws ParsingException {
        String videoId = shortsLockupViewModel.getObject("onTap")
                .getObject("innertubeCommand")
                .getObject("reelWatchEndpoint")
                .getString("videoId");

        if (isNullOrEmpty(videoId)) {
            videoId = shortsLockupViewModel.getObject("inlinePlayerData")
                    .getObject("onVisible")
                    .getObject("innertubeCommand")
                    .getObject("watchEndpoint")
                    .getString("videoId");
        }

        if (isNullOrEmpty(videoId)) {
            throw new ParsingException("Could not get video ID");
        }

        try {
            return YoutubeStreamLinkHandlerFactory.getInstance().getUrl(videoId);
        } catch (final Exception e) {
            throw new ParsingException("Could not get URL", e);
        }
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        return getImagesFromThumbnailsArray(shortsLockupViewModel.getObject("thumbnail")
                .getArray("sources"));
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public long getViewCount() throws ParsingException {
        final String viewCountText = shortsLockupViewModel.getObject("overlayMetadata")
                        .getObject("secondaryText")
                        .getString("content");
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

    // All the following properties cannot be obtained from shortsLockupViewModels

    @Override
    public boolean isAd() throws ParsingException {
        return false;
    }

    @Override
    public long getDuration() throws ParsingException {
        return -1;
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
