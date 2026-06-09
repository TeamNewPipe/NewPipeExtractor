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
import org.schabi.newpipe.extractor.stream.ContentAvailability;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Extractor of YouTube lockup view models for stream items.
 *
 * <p>
 *     The following features are currently not implemented:
 *     <ul>
 *         <li>Shorts: appear in related items without a duration badge; getDuration() returns
 *         -1</li>
 *         <li>YouTube Premium Paid content</li>
 *     </ul>
 * </p>
 */
public class YoutubeStreamInfoItemLockupExtractor implements StreamInfoItemExtractor {

    private static final String NO_VIEWS_LOWERCASE = "no views";
    // This approach is language dependant (en-GB)
    // Leading end space is voluntary included
    private static final String PREMIERES_VIDEOS_TEXT = "Premieres ";
    private static final String PREMIERES_LIVES_TEXT = "Scheduled for ";
    private static final DateTimeFormatter PREMIERES_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");

    private final JsonObject lockupViewModel;
    private final TimeAgoParser timeAgoParser;
    private final JsonArray cachedMetadataRows;

    private StreamType cachedStreamType;
    private String cachedName;
    private String cachedDateText;

    private ChannelImageViewModel cachedChannelImageViewModel;

    /**
     * Creates an extractor of StreamInfoItems from a YouTube page.
     *
     * @param lockupViewModel The JSON page element
     * @param timeAgoParser A parser of the textual dates or {@code null}.
     */
    public YoutubeStreamInfoItemLockupExtractor(@Nonnull final JsonObject lockupViewModel,
                                                @Nullable final TimeAgoParser timeAgoParser) {
        this.lockupViewModel = lockupViewModel;
        this.timeAgoParser = timeAgoParser;
        cachedMetadataRows = lockupViewModel.getObject("metadata")
                .getObject("lockupMetadataViewModel")
                .getObject("metadata")
                .getObject("contentMetadataViewModel")
                .getArray("metadataRows");
    }

    /**
     * Returns whether this is a lockup view model for a channel or a course playlist.
     *
     * <p>
     * Some cases to parse properly the date and the views count requires to know this.
     * </p>
     *
     * @return whether this is a lockup view model for a channel or a course playlist, false by
     * default
     */
    protected boolean isChannelOrCoursePlaylistLockupItem() {
        return false;
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        if (cachedStreamType == null) {
            cachedStreamType = determineStreamType();
        }
        return cachedStreamType;
    }

    private StreamType determineStreamType() throws ParsingException {
        final JsonArray overlays = JsonUtils.getArray(lockupViewModel,
            "contentImage.thumbnailViewModel.overlays");

        // thumbnailOverlayBadgeViewModel path (legacy/alternate overlay structure)
        if (overlays.streamAsJsonObjects()
            .flatMap(overlay -> overlay
                .getObject("thumbnailOverlayBadgeViewModel")
                .getArray("thumbnailBadges")
                .streamAsJsonObjects())
            .map(thumbnailBadge -> thumbnailBadge.getObject("thumbnailBadgeViewModel"))
            .anyMatch(vm -> {
                if ("THUMBNAIL_OVERLAY_BADGE_STYLE_LIVE".equals(vm.getString("badgeStyle"))) {
                    return true;
                }
                // Fallback: Check if there is a live icon
                return vm.getObject("icon")
                    .getArray("sources")
                    .streamAsJsonObjects()
                    .map(source -> source
                        .getObject("clientResource")
                        .getString("imageName"))
                    .anyMatch("LIVE"::equals);
            })) {
            return StreamType.LIVE_STREAM;
        }

        // thumbnailBottomOverlayViewModel path (used in lockup format for both duration and live)
        if (overlays.streamAsJsonObjects()
            .flatMap(overlay -> overlay
                .getObject("thumbnailBottomOverlayViewModel")
                .getArray("badges")
                .streamAsJsonObjects())
            .map(badge -> badge.getObject("thumbnailBadgeViewModel"))
            .anyMatch(vm -> "THUMBNAIL_OVERLAY_BADGE_STYLE_LIVE".equals(
                vm.getString("badgeStyle")))) {
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
        // Exact duration cannot be extracted for premieres, an approximation is only available in
        // accessibility context label
        if (isLive() || isPremiere()) {
            return -1;
        }

        final List<String> potentialDurations = JsonUtils.getArray(lockupViewModel,
                "contentImage.thumbnailViewModel.overlays")
            .streamAsJsonObjects()
            .flatMap(jsonObject -> jsonObject
                .getObject("thumbnailBottomOverlayViewModel")
                .getArray("badges")
                .streamAsJsonObjects())
            .map(jsonObject -> jsonObject
                .getObject("thumbnailBadgeViewModel")
                .getString("text"))
            .collect(Collectors.toList());

        if (potentialDurations.isEmpty()) {
            return -1;
        }

        ParsingException parsingException = null;
        for (final String potentialDuration : potentialDurations) {
            if (potentialDuration == null || !potentialDuration.matches(".*\\d.*")) {
                continue;
            }
            try {
                return YoutubeParsingHelper.parseDurationString(potentialDuration);
            } catch (final ParsingException ex) {
                parsingException = ex;
            }
        }

        if (parsingException == null) {
            return -1; // e.g. only "SHORTS" or "CC" badge was present, no duration available
        }

        throw new ParsingException("Could not get duration", parsingException);
    }

    @Override
    public String getUploaderName() throws ParsingException {
        final List<JsonArray> metadataRows = getMetadataPartsFromMetadataRows();
        if (metadataRows.isEmpty()) {
            throw new ParsingException("Could not get uploader name: no metadata row");
        }

        final String uploaderName = getTextContentFromMetadataPart(metadataRows.get(0)
                .getObject(0));
        if (isNullOrEmpty(uploaderName)) {
            throw new ParsingException("Could not get uploader name");
        }

        return uploaderName;
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        final JsonObject innerTubeCommand = channelImageViewModel()
            .forUploaderUrlExtraction()
            .getObject("rendererContext")
            .getObject("commandContext")
            .getObject("onTap")
            .getObject("innertubeCommand");
        final JsonObject browseEndpoint = innerTubeCommand
            .getObject("browseEndpoint");
        final String channelId = browseEndpoint
            .getString("browseId");

        if (channelId != null && channelId.startsWith("UC")) {
            return YoutubeChannelLinkHandlerFactory.getInstance().getUrl("channel/" + channelId);
        }

        final String canonicalBaseUrl = browseEndpoint.getString("canonicalBaseUrl");
        if (!isNullOrEmpty(canonicalBaseUrl)) {
            return resolveUploaderUrlFromRelativeUrl(canonicalBaseUrl);
        }

        final String webCommandMetadataUrl = innerTubeCommand.getObject("commandMetadata")
            .getObject("webCommandMetadata")
            .getString("url");
        if (!isNullOrEmpty(webCommandMetadataUrl)) {
            return resolveUploaderUrlFromRelativeUrl(webCommandMetadataUrl);
        }

        throw new ParsingException("Could not get uploader url");
    }

    private String resolveUploaderUrlFromRelativeUrl(@Nonnull final String relativeUrl)
        throws ParsingException {
        return YoutubeChannelLinkHandlerFactory.getInstance().getUrl(
            relativeUrl.startsWith("/") ? relativeUrl.substring(1) : relativeUrl);
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
        final List<JsonArray> metadataRows = getMetadataPartsFromMetadataRows();
        if (metadataRows.isEmpty()) {
            throw new ParsingException("Could not get uploader verified status: no metadata row");
        }

        return YoutubeParsingHelper.hasArtistOrVerifiedIconBadgeAttachment(metadataRows.get(0)
                .getObject(0)
                .getObject("text")
                .getArray("attachmentRuns"));
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        // Live streams have no upload date
        if (isLive()) {
            return null;
        }

        // Date string might be null e.g. for live streams
        final String dateText = getDateText();

        if (isPremiere()) {
            return getDateFromPremiere(dateText);
        }

        return dateText;
    }

    @Nonnull
    private String getDateFromPremiere(@Nonnull final String dateText) {
        // This approach is language dependent
        // Remove the premieres text from the upload date metadata part
        return dateText.replace(PREMIERES_VIDEOS_TEXT, "")
                        .replace(PREMIERES_LIVES_TEXT, "");
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

        if (isPremiere()) {
            final String premiereDate = getDateFromPremiere(getDateText());

            try {
                // As we request a UTC offset of 0 minutes, we get the UTC date
                final var dateTime = LocalDateTime.parse(premiereDate, PREMIERES_DATE_FORMATTER);
                return new DateWrapper(dateTime.atZone(ZoneOffset.UTC).toInstant(), false);
            } catch (final DateTimeParseException e) {
                throw new ParsingException("Could not parse premiere upload date", e);
            }
        }

        return timeAgoParser.parse(textualUploadDate);
    }

    @Override
    public long getViewCount() throws ParsingException {
        if (isChannelsMembersOnlyOrFirst()) {
            // Members only or members first contents do not return their view count
            // Check done here as there should be no metadata row for running members-only
            // livestreams on channels
            return -1;
        }

        final List<JsonArray> metadataPartsRows = getMetadataPartsFromMetadataRows();
        if (metadataPartsRows.isEmpty()) {
            // No metadata part row is returned for running livestreams with no viewers on channels
            // (course playlists shouldn't have livestreams)
            if (isLive() && isChannelOrCoursePlaylistLockupItem()) {
                return 0;
            }

            throw new ParsingException(
                    "Could not get view count: no metadata part from metadata rows");
        }

        if (isPremiere()) {
            // The number of people returned for premieres is the one currently waiting
            // Check done here as isPremiere relies on metadataPartsRows
            return -1;
        }

        if (isLive() && metadataPartsRows.size() == 1 && !isChannelOrCoursePlaylistLockupItem()) {
            // If there is only one metadata part on channel lockup items for running livestreams,
            // this should be the watching count (course playlists shouldn't have livestreams)
            // If this isn't a channel lockup item, this should be a livestream without any viewer
            return 0;
        }

        /*
         * YouTube uses 2 rows for stream items outside channels and course playlists: one for
         * author(s) then one for views and upload date (in standard cases for this row). However,
         * on channels, it uses mostly 1 row except for collaborations when there are two, but the
         * views and upload date metadata row is always the latest one.
         */
        final JsonArray metadataPartsRow = metadataPartsRows.get(metadataPartsRows.size() - 1);
        if (metadataPartsRow.isEmpty()) {
            throw new ParsingException(
                    "Could not get view count: no metadata part in the metadata parts array");
        }

        // View count is always returned as the first metadata part, then date text
        // For members only content, view count isn't returned, so there is only the date text
        // in the metadata row
        final String viewCountText = getTextContentFromMetadataPart(
                metadataPartsRow.getObject(0));
        if (isNullOrEmpty(viewCountText)) {
            throw new ParsingException("Could not get view count");
        }
        return getViewCountFromViewCountText(viewCountText);
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

    @Nonnull
    @Override
    public ContentAvailability getContentAvailability() throws ParsingException {
        if (isChannelsMembersOnlyOrFirst()) {
            // In the case we get a running members-only livestream, checking for isLive first
            // would return an incorrect content availability
            // This case hasn't been found when this code has been written, so it needs to be
            // checked
            return ContentAvailability.MEMBERSHIP;
        }

        if (isLive()) {
            // Check that it is a running livestream first as in the case of a livestream, no date
            // text is available so getDateText called isPremiere will throw an exception
            return ContentAvailability.AVAILABLE;
        }

        if (isPremiere()) {
            return ContentAvailability.UPCOMING;
        }

        return ContentAvailability.AVAILABLE;
    }

    private ChannelImageViewModel channelImageViewModel() throws ParsingException {
        if (cachedChannelImageViewModel == null) {
            cachedChannelImageViewModel = determineChannelImageViewModel();
        }

        return cachedChannelImageViewModel;
    }

    @Nonnull
    private ChannelImageViewModel determineChannelImageViewModel() throws ParsingException {
        final JsonObject image = lockupViewModel.getObject("metadata")
                .getObject("lockupMetadataViewModel")
                .getObject("image");

        final JsonObject single = image.getObject("decoratedAvatarViewModel", null);
        if (single != null) {
            return new SingleChannelImageViewModel(single);
        }

        final JsonObject multi = image.getObject("avatarStackViewModel", null);
        if (multi != null) {
            return new MultiChannelImageViewModel(multi);
        }

        throw new ParsingException("Failed to determine channel image view model");
    }

    @Nullable
    private String getTextContentFromMetadataPart(@Nonnull final JsonObject metadataPart) {
        return metadataPart.getObject("text")
                .getString("content");
    }

    private boolean isLive() throws ParsingException {
        return getStreamType() != StreamType.VIDEO_STREAM;
    }

    private boolean isChannelsMembersOnlyOrFirst() {
        return cachedMetadataRows.streamAsJsonObjects()
                .flatMap(jsonObject -> jsonObject.getArray("badges")
                        .streamAsJsonObjects())
                .map(badge -> badge.getObject("badgeViewModel")
                        .getString("badgeStyle"))
                // Also returned for members first contents
                .anyMatch("BADGE_MEMBERS_ONLY"::equals);
    }

    private boolean isPremiere() throws ParsingException {
        final String dateText = getDateText();
        return dateText.contains(PREMIERES_VIDEOS_TEXT) || dateText.contains(PREMIERES_LIVES_TEXT);
    }

    private String getDateText() throws ParsingException {
        if (cachedDateText == null) {
            final List<JsonArray> metadataPartsRows = getMetadataPartsFromMetadataRows();
            if (metadataPartsRows.isEmpty()) {
                throw new ParsingException(
                        "Could not get date text: no metadata part from metadata rows");
            }

            /*
             * YouTube uses 2 rows for stream items outside channels and course playlists: author(s)
             * then one for views and upload date (in standard cases for this row). However, on
             * channels, this is mostly 1 row except for collaborations when there are two, but the
             * views and upload date metadata row is always the latest one.
             */
            final JsonArray metadataPartsRow = metadataPartsRows.get(metadataPartsRows.size() - 1);
            if (metadataPartsRow.isEmpty()) {
                throw new ParsingException(
                        "Could not get date text: no metadata part in the metadata parts array");
            }

            // View count is always returned as the first metadata part, then date text
            // For members only content, view count isn't returned, so there is only the date text
            // in the metadata row
            cachedDateText = getTextContentFromMetadataPart(
                    metadataPartsRow.getObject(metadataPartsRow.size() - 1));
            return cachedDateText;
        }
        return cachedDateText;
    }

    @Nonnull
    private List<JsonArray> getMetadataPartsFromMetadataRows() {
        final List<JsonArray> metadataParts = new ArrayList<>();

        for (int i = 0; i < cachedMetadataRows.size(); i++) {
            final JsonObject metadataRow = cachedMetadataRows.getObject(i);
            if (metadataRow.has("metadataParts")) {
                metadataParts.add(metadataRow.getArray("metadataParts"));
            }
        }

        return metadataParts;
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
            return viewModel.getObject("rendererContext")
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
