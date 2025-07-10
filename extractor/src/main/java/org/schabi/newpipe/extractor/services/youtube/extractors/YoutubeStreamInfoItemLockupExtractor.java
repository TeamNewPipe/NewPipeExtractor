package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Note:
 * This extractor is currently (2025-07) only used to extract related video streams.<br/>
 * The following features are currently not implemented because they have never been observed:
 * <ul>
 *     <li>Shorts</li>
 *     <li>Premiers</li>
 *     <li>Premium content</li>
 * </ul>
 */
public class YoutubeStreamInfoItemLockupExtractor implements StreamInfoItemExtractor {

    private static final String NO_VIEWS_LOWERCASE = "no views";

    private final JsonObject lockupViewModel;
    private final TimeAgoParser timeAgoParser;

    private String cachedName;
    private String cachedTextualUploadDate;

    private JsonArray cachedMetadataRows;

    /**
     * Creates an extractor of StreamInfoItems from a YouTube page.
     *
     * @param lockupViewModel The JSON page element
     * @param timeAgoParser A parser of the textual dates or {@code null}.
     */
    public YoutubeStreamInfoItemLockupExtractor(final JsonObject lockupViewModel,
                                                @Nullable final TimeAgoParser timeAgoParser) {
        this.lockupViewModel = lockupViewModel;
        this.timeAgoParser = timeAgoParser;
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        if (JsonUtils.getArray(lockupViewModel, "contentImage.thumbnailViewModel.overlays")
            .streamAsJsonObjects()
            .flatMap(overlay -> overlay
                .getObject("thumbnailOverlayBadgeViewModel")
                .getArray("thumbnailBadges")
                .streamAsJsonObjects())
            .map(thumbnailBadge -> thumbnailBadge.getObject("thumbnailBadgeViewModel"))
            .anyMatch(thumbnailBadgeViewModel -> {
                if ("THUMBNAIL_OVERLAY_BADGE_STYLE_LIVE".equals(
                    thumbnailBadgeViewModel.getString("badgeStyle"))) {
                    return true;
                }

                // Fallback: Check if there is a live icon
                return thumbnailBadgeViewModel
                    .getObject("icon")
                    .getArray("sources")
                    .streamAsJsonObjects()
                    .map(source -> source
                        .getObject("clientResource")
                        .getString("imageName"))
                    .anyMatch("LIVE"::equals);
            })) {
            return StreamType.LIVE_STREAM;
        }

        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() throws ParsingException {
        final String name = getName(); // only get it once
        return "[Private video]".equals(name)
            || "[Deleted video]".equals(name);
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            String videoId = lockupViewModel.getString("contentId");
            if (isNullOrEmpty(videoId)) {
                videoId = JsonUtils.getString(lockupViewModel,
                    "rendererContext.commandContext.onTap.innertubeCommand.watchEndpoint.videoId");
            }
            return YoutubeStreamLinkHandlerFactory.getInstance().getUrl(videoId);
        } catch (final Exception e) {
            throw new ParsingException("Could not get url", e);
        }
    }

    @Override
    public String getName() throws ParsingException {
        if (cachedName != null) {
            return cachedName;
        }

        final String name = JsonUtils.getString(lockupViewModel,
            "metadata.lockupMetadataViewModel.title.content");
        if (!isNullOrEmpty(name)) {
            this.cachedName = name;
            return name;
        }
        throw new ParsingException("Could not get name");
    }

    @Override
    public long getDuration() throws ParsingException {
        final List<String> potentialDurations = JsonUtils.getArray(lockupViewModel,
                "contentImage.thumbnailViewModel.overlays")
            .streamAsJsonObjects()
            .flatMap(jsonObject -> jsonObject
                .getObject("thumbnailOverlayBadgeViewModel")
                .getArray("thumbnailBadges")
                .streamAsJsonObjects())
            .map(jsonObject -> jsonObject
                .getObject("thumbnailBadgeViewModel")
                .getString("text"))
            .collect(Collectors.toList());

        if (potentialDurations.isEmpty()) {
            throw new ParsingException("Could not get duration: No parsable durations detected");
        }

        ParsingException parsingException = null;
        for (final String potentialDuration : potentialDurations) {
            try {
                return YoutubeParsingHelper.parseDurationString(potentialDuration);
            } catch (final ParsingException ex) {
                parsingException = ex;
            }
        }

        throw new ParsingException("Could not get duration", parsingException);
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return metadataPart(0, 0)
            .map(this::getTextContentFromMetadataPart)
            .filter(s -> !isNullOrEmpty(s))
            .orElseThrow(() -> new ParsingException("Could not get uploader name"));
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        final String channelId = JsonUtils.getString(lockupViewModel,
            "metadata.lockupMetadataViewModel.image.decoratedAvatarViewModel"
                + ".rendererContext.commandContext.onTap"
                + ".innertubeCommand.browseEndpoint.browseId");
        if (isNullOrEmpty(channelId)) {
            throw new ParsingException("Could not get uploader url");
        }
        return YoutubeChannelLinkHandlerFactory.getInstance().getUrl(channelId);
    }

    @Nonnull
    @Override
    public List<Image> getUploaderAvatars() throws ParsingException {
        return YoutubeParsingHelper.getImagesFromThumbnailsArray(
            JsonUtils.getArray(lockupViewModel,
                "metadata.lockupMetadataViewModel.image.decoratedAvatarViewModel"
                    + ".avatar.avatarViewModel.image.sources"));
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return metadataPart(0, 0)
            .map(jsonObject -> jsonObject
                .getObject("text")
                .getArray("attachmentRuns"))
            .map(YoutubeParsingHelper::hasArtistOrVerifiedIconBadgeAttachment)
            .orElse(false);
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        if (cachedTextualUploadDate != null) {
            return cachedTextualUploadDate;
        }

        this.cachedTextualUploadDate = metadataPart(1, 1)
            .map(this::getTextContentFromMetadataPart)
            .orElse(null);
        return cachedTextualUploadDate;
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        if (timeAgoParser == null) {
            return null;
        }

        return timeAgoParser.parse(getTextualUploadDate());
    }

    @Override
    public long getViewCount() throws ParsingException {
        final Optional<String> optTextContent = metadataPart(1, 0)
            .map(this::getTextContentFromMetadataPart);
        // We could do this inline if the ParsingException would be a RuntimeException -.-
        if (optTextContent.isPresent()) {
            return getViewCountFromViewCountText(optTextContent.get());
        }
        return -1;
    }

    private long getViewCountFromViewCountText(@Nonnull final String viewCountText)
            throws NumberFormatException, ParsingException {
        // These approaches are language dependent
        if (viewCountText.toLowerCase().contains(NO_VIEWS_LOWERCASE)) {
            return 0;
        } else if (viewCountText.toLowerCase().contains("recommended")) {
            return -1;
        }

        return Utils.mixedNumberWordToLong(viewCountText);
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        return YoutubeParsingHelper.getImagesFromThumbnailsArray(
            JsonUtils.getArray(lockupViewModel,
                "contentImage.thumbnailViewModel.image.sources"));
    }

    private Optional<JsonObject> metadataPart(final int rowIndex, final int partIndex)
        throws ParsingException {
        if (cachedMetadataRows == null) {
            cachedMetadataRows = JsonUtils.getArray(lockupViewModel,
                "metadata.lockupMetadataViewModel.metadata"
                    + ".contentMetadataViewModel.metadataRows");
        }
        return cachedMetadataRows
            .streamAsJsonObjects()
            .skip(rowIndex)
            .limit(1)
            .flatMap(jsonObject -> jsonObject.getArray("metadataParts")
                .streamAsJsonObjects()
                .skip(partIndex)
                .limit(1))
            .findFirst();
    }

    private String getTextContentFromMetadataPart(final JsonObject metadataPart) {
        return metadataPart.getObject("text").getString("content");
    }
}
