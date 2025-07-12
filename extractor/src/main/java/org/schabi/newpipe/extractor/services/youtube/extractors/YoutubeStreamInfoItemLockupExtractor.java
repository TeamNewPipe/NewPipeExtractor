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

import java.util.ArrayList;
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
 *     <li>Premieres</li>
 *     <li>Paid content (Premium, members first or only)</li>
 * </ul>
 */
public class YoutubeStreamInfoItemLockupExtractor implements StreamInfoItemExtractor {

    private static final String NO_VIEWS_LOWERCASE = "no views";

    private final JsonObject lockupViewModel;
    private final TimeAgoParser timeAgoParser;

    private StreamType cachedStreamType;
    private String cachedName;
    private Optional<String> cachedTextualUploadDate;

    private ChannelImageViewModel cachedChannelImageViewModel;
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
        if (cachedStreamType == null) {
            cachedStreamType = determineStreamType();
        }
        return cachedStreamType;
    }

    private StreamType determineStreamType() throws ParsingException {
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
        final String name = getName();
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
        // Duration cannot be extracted for live streams, but only for normal videos
        if (isLive()) {
            return -1;
        }

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
        final String channelId = channelImageViewModel()
            .forUploaderUrlExtraction()
            .getObject("rendererContext")
            .getObject("commandContext")
            .getObject("onTap")
            .getObject("innertubeCommand")
            .getObject("browseEndpoint")
            .getString("browseId");

        if (isNullOrEmpty(channelId)) {
            throw new ParsingException("Could not get uploader url");
        }
        return YoutubeChannelLinkHandlerFactory.getInstance().getUrl(channelId);
    }

    @Nonnull
    @Override
    public List<Image> getUploaderAvatars() throws ParsingException {
        return YoutubeParsingHelper.getImagesFromThumbnailsArray(
            JsonUtils.getArray(
                channelImageViewModel().forAvatarExtraction(),
                "avatarViewModel.image.sources"));
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
            return cachedTextualUploadDate.orElse(null);
        }

        // Live streams have no upload date
        if (isLive()) {
            cachedTextualUploadDate = Optional.empty();
            return null;
        }

        // This might be null e.g. for live streams
        this.cachedTextualUploadDate = metadataPart(1, 1)
            .map(this::getTextContentFromMetadataPart);
        return cachedTextualUploadDate.orElse(null);
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        if (timeAgoParser == null) {
            return null;
        }

        final String textualUploadDate = getTextualUploadDate();
        // Prevent NPE when e.g. a live stream is shown
        if (textualUploadDate == null) {
            return null;
        }
        return timeAgoParser.parse(textualUploadDate);
    }

    @Override
    public long getViewCount() throws ParsingException {
        final Optional<String> optTextContent = metadataPart(1, 0)
            .map(this::getTextContentFromMetadataPart);
        // We could do this inline if the ParsingException would be a RuntimeException -.-
        if (optTextContent.isPresent()) {
            return getViewCountFromViewCountText(optTextContent.get());
        }
        return !isLive()
            ? -1
            // Live streams don't have the metadata row present if there are 0 viewers
            // https://github.com/TeamNewPipe/NewPipeExtractor/pull/1320#discussion_r2205837528
            : 0;
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

    private ChannelImageViewModel channelImageViewModel() throws ParsingException {
        if (cachedChannelImageViewModel == null) {
            cachedChannelImageViewModel = determineChannelImageViewModel();
        }

        return cachedChannelImageViewModel;
    }

    private ChannelImageViewModel determineChannelImageViewModel() throws ParsingException {
        final JsonObject image = lockupViewModel
            .getObject("metadata")
            .getObject("lockupMetadataViewModel")
            .getObject("image");

        final JsonObject single = image
            .getObject("decoratedAvatarViewModel", null);
        if (single != null) {
            return new SingleChannelImageViewModel(single);
        }

        final JsonObject multi = image.getObject("avatarStackViewModel", null);
        if (multi != null) {
            return new MultiChannelImageViewModel(multi);
        }

        throw new ParsingException("Failed to determine channel image view model");
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

    private boolean isLive() throws ParsingException {
        return getStreamType() != StreamType.VIDEO_STREAM;
    }

    abstract static class ChannelImageViewModel {
        protected JsonObject viewModel;

        protected ChannelImageViewModel(final JsonObject viewModel) {
            this.viewModel = viewModel;
        }

        public abstract JsonObject forUploaderUrlExtraction();

        public abstract JsonObject forAvatarExtraction();
    }

    static class SingleChannelImageViewModel extends ChannelImageViewModel {
        SingleChannelImageViewModel(final JsonObject viewModel) {
            super(viewModel);
        }

        @Override
        public JsonObject forUploaderUrlExtraction() {
            return viewModel;
        }

        @Override
        public JsonObject forAvatarExtraction() {
            return viewModel.getObject("avatar");
        }
    }

    static class MultiChannelImageViewModel extends ChannelImageViewModel {
        MultiChannelImageViewModel(final JsonObject viewModel) {
            super(viewModel);
        }

        @Override
        public JsonObject forUploaderUrlExtraction() {
            return viewModel
                .getObject("rendererContext")
                .getObject("commandContext")
                .getObject("onTap")
                .getObject("innertubeCommand")
                .getObject("showDialogCommand")
                .getObject("panelLoadingStrategy")
                .getObject("inlineContent")
                .getObject("dialogViewModel")
                .getObject("customContent")
                .getObject("listViewModel")
                .getArray("listItems")
                .streamAsJsonObjects()
                .map(item -> item.getObject("listItemViewModel"))
                .findFirst()
                .orElse(null);
        }

        @Override
        public JsonObject forAvatarExtraction() {
            return viewModel.getArray("avatars")
                .getObject(0);
        }
    }
}
