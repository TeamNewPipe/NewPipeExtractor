package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

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

public class YoutubeStreamInfoItemLockupExtractor implements StreamInfoItemExtractor {

    private static final String NO_VIEWS_LOWERCASE = "no views";

    private final JsonObject lockupViewModel;
    private final TimeAgoParser timeAgoParser;

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
    public StreamType getStreamType() {
        // TODO only encountered video streams so far... Are there more types?
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() throws ParsingException {
        if (isPremium()) {
            return true;
        }
        final String name = getName(); // only get it once
        return "[Private video]".equals(name)
            || "[Deleted video]".equals(name);
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            final String videoId = lockupViewModel.getString("contentId");
            return YoutubeStreamLinkHandlerFactory.getInstance().getUrl(videoId);
        } catch (final Exception e) {
            throw new ParsingException("Could not get url", e);
        }
    }

    @Override
    public String getName() throws ParsingException {
        final String name = JsonUtils.getString(lockupViewModel,
            "metadata.lockupMetadataViewModel.title.content");
        if (!isNullOrEmpty(name)) {
            return name;
        }
        throw new ParsingException("Could not get name");
    }

    @Override
    public long getDuration() throws ParsingException {
        final List<String> potentialDurations = lockupViewModel
            .getObject("contentImage")
            .getObject("thumbnailViewModel")
            .getArray("overlays")
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
            .stream()
            .flatMap(jsonObject -> jsonObject
                .getObject("text")
                .getArray("attachmentRuns")
                .streamAsJsonObjects())
            .flatMap(jsonObject -> jsonObject
                .getObject("element")
                .getObject("type")
                .getObject("imageType")
                .getObject("image")
                .getArray("sources")
                .streamAsJsonObjects())
            .map(jsonObject -> jsonObject
                .getObject("clientResource")
                .getString("imageName"))
            .map("CHECK_CIRCLE_FILLED"::equals)
            .findFirst()
            .orElse(false);
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        return metadataPart(1, 1)
            .map(this::getTextContentFromMetadataPart)
            .orElse(null);
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
        if (isPremium() || isPremiere()) {
            return -1;
        }

        // TODO Check if this is the same for shorts
        final Optional<String> optTextContent = metadataPart(1, 0)
            .map(this::getTextContentFromMetadataPart);
        // We could do this inline if the ParsingException would be a RuntimeException -.-
        if (optTextContent.isPresent()) {
            return getViewCountFromViewCountText(optTextContent.get());
        }
        return -1;
    }

    protected long getViewCountFromViewCountText(@Nonnull final String viewCountText)
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

    protected boolean isPremium() {
        // TODO Detect with samples
        return false;
    }

    protected boolean isPremiere() {
        // TODO Detect with samples
        return false;
    }

    protected Optional<JsonObject> metadataPart(final int rowIndex, final int partIndex)
        throws ParsingException {
        return JsonUtils.getArray(lockupViewModel,
                "metadata.lockupMetadataViewModel.metadata"
                    + ".contentMetadataViewModel.metadataRows")
            .streamAsJsonObjects()
            .skip(rowIndex)
            .limit(1)
            .flatMap(jsonObject -> jsonObject.getArray("metadataParts")
                .streamAsJsonObjects()
                .skip(partIndex)
                .limit(1))
            .findFirst();
    }

    protected String getTextContentFromMetadataPart(final JsonObject metadataPart) {
        return metadataPart.getObject("text").getString("content");
    }

    @Nullable
    @Override
    public String getShortDescription() throws ParsingException {
        return null;
    }

    @Override
    public boolean isShortFormContent() throws ParsingException {
        // TODO Detect with samples
        return false;
    }
}
