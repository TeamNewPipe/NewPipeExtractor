/*
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeStreamInfoItemExtractor.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObjectOrThrow;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getThumbnailsFromInfoItem;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.ContentAvailability;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class YoutubeStreamInfoItemExtractor implements StreamInfoItemExtractor {

    private static final Pattern ACCESSIBILITY_DATA_VIEW_COUNT_REGEX =
            Pattern.compile("([\\d,]+) views$");
    private static final String NO_VIEWS_LOWERCASE = "no views";

    private final JsonObject videoInfo;
    private final TimeAgoParser timeAgoParser;
    private StreamType cachedStreamType;
    private Boolean isPremiere;

    /**
     * Creates an extractor of StreamInfoItems from a YouTube page.
     *
     * @param videoInfoItem The JSON page element
     * @param timeAgoParser A parser of the textual dates or {@code null}.
     */
    public YoutubeStreamInfoItemExtractor(final JsonObject videoInfoItem,
                                          @Nullable final TimeAgoParser timeAgoParser) {
        this.videoInfo = videoInfoItem;
        this.timeAgoParser = timeAgoParser;
    }

    @Override
    public StreamType getStreamType() {
        if (cachedStreamType != null) {
            return cachedStreamType;
        }

        final JsonArray badges = videoInfo.getArray("badges");
        for (final Object badge : badges) {
            if (!(badge instanceof JsonObject)) {
                continue;
            }

            final JsonObject badgeRenderer
                    = ((JsonObject) badge).getObject("metadataBadgeRenderer");
            if (badgeRenderer.getString("style", "").equals("BADGE_STYLE_TYPE_LIVE_NOW")
                    || badgeRenderer.getString("label", "").equals("LIVE NOW")) {
                cachedStreamType = StreamType.LIVE_STREAM;
                return cachedStreamType;
            }
        }

        for (final Object overlay : videoInfo.getArray("thumbnailOverlays")) {
            if (!(overlay instanceof JsonObject)) {
                continue;
            }

            final String style = ((JsonObject) overlay)
                    .getObject("thumbnailOverlayTimeStatusRenderer")
                    .getString("style", "");
            if (style.equalsIgnoreCase("LIVE")) {
                cachedStreamType = StreamType.LIVE_STREAM;
                return cachedStreamType;
            }
        }

        cachedStreamType = StreamType.VIDEO_STREAM;
        return cachedStreamType;
    }

    @Override
    public boolean isAd() throws ParsingException {
        return isPremium() || getName().equals("[Private video]")
                || getName().equals("[Deleted video]");
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            final String videoId = videoInfo.getString("videoId");
            return YoutubeStreamLinkHandlerFactory.getInstance().getUrl(videoId);
        } catch (final Exception e) {
            throw new ParsingException("Could not get url", e);
        }
    }

    @Override
    public String getName() throws ParsingException {
        return getTextFromObjectOrThrow(videoInfo.getObject("title"), "name");
    }

    @Override
    public long getDuration() throws ParsingException {
        if (getStreamType() == StreamType.LIVE_STREAM) {
            return -1;
        }

        final String duration = getTextFromObject(videoInfo.getObject("lengthText"))
                // Available in playlists for videos
                .or(() -> Optional.ofNullable(videoInfo.getString("lengthSeconds")))
                .or(() -> videoInfo.getArray("thumbnailOverlays").streamAsJsonObjects()
                        .map(overlay -> overlay.getObject("thumbnailOverlayTimeStatusRenderer"))
                        .filter(renderer -> !renderer.isEmpty())
                        .findFirst()
                        .flatMap(overlay -> getTextFromObject(overlay.getObject("text"))))
                .orElse(null);

        if (isNullOrEmpty(duration)) {
            if (isPremiere()) {
                // Premieres can be livestreams, so the duration is not available in this
                // case
                return -1;
            }

            throw new ParsingException("Could not get duration");
        }

        return YoutubeParsingHelper.parseDurationString(duration);
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return getTextFromObject(videoInfo.getObject("longBylineText"))
                .or(() -> getTextFromObject(videoInfo.getObject("ownerText")))
                .or(() -> getTextFromObject(videoInfo.getObject("shortBylineText")))
                .orElseThrow(() -> new ParsingException("Could not get uploader name"));
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return getUrlFromNavigationEndpoint(videoInfo.getObject("longBylineText"))
                .or(() -> getUrlFromNavigationEndpoint(videoInfo.getObject("ownerText")))
                .or(() -> getUrlFromNavigationEndpoint(videoInfo.getObject("shortBylineText")))
                .orElseThrow(() -> new ParsingException("Could not get uploader url"));
    }

    @Nonnull
    private Optional<String> getUrlFromNavigationEndpoint(@Nonnull final JsonObject jsonObject) {
        final var endpoint = jsonObject.getArray("runs").getObject(0)
                .getObject("navigationEndpoint");
        return YoutubeParsingHelper.getUrlFromNavigationEndpoint(endpoint);
    }

    @Nonnull
    @Override
    public List<Image> getUploaderAvatars() throws ParsingException {
        if (videoInfo.has("channelThumbnailSupportedRenderers")) {
            return getImagesFromThumbnailsArray(JsonUtils.getArray(videoInfo,
                    // CHECKSTYLE:OFF
                    "channelThumbnailSupportedRenderers.channelThumbnailWithLinkRenderer.thumbnail.thumbnails"));
                    // CHECKSTYLE:ON
        }

        if (videoInfo.has("channelThumbnail")) {
            return getImagesFromThumbnailsArray(
                    JsonUtils.getArray(videoInfo, "channelThumbnail.thumbnails"));
        }

        return List.of();
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return YoutubeParsingHelper.isVerified(videoInfo.getArray("ownerBadges"));
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        if (getStreamType() == StreamType.LIVE_STREAM) {
            return null;
        }

        if (isPremiere()) {
            final var localDateTime = LocalDateTime.ofInstant(getInstantFromPremiere(),
                    ZoneId.systemDefault());
            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(localDateTime);
        }

        return getTextFromObject(videoInfo.getObject("publishedTimeText"))
                .or(() -> {
                    // Returned in playlists, in the form: view count separator upload date
                    return Optional.ofNullable(videoInfo.getObject("videoInfo")
                            .getArray("runs")
                            .getObject(2)
                            .getString("text"));
                })
                .orElse(null);
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        if (getStreamType() == StreamType.LIVE_STREAM) {
            return null;
        }

        if (isPremiere()) {
            return new DateWrapper(getInstantFromPremiere());
        }

        final String textualUploadDate = getTextualUploadDate();
        if (timeAgoParser != null && !isNullOrEmpty(textualUploadDate)) {
            try {
                return timeAgoParser.parse(textualUploadDate);
            } catch (final ParsingException e) {
                throw new ParsingException("Could not get upload date", e);
            }
        }
        return null;
    }

    @Override
    public long getViewCount() throws ParsingException {
        if (isPremium() || isPremiere()) {
            return -1;
        }

        // Ignore all exceptions, as the view count can be hidden by creators, and so cannot be
        // found in this case

        final String viewCountText = getTextFromObject(videoInfo.getObject("viewCountText"))
                .orElse(null);
        if (!isNullOrEmpty(viewCountText)) {
            try {
                return getViewCountFromViewCountText(viewCountText, false);
            } catch (final Exception ignored) {
            }
        }

        // Try parsing the real view count from accessibility data, if that's not a running
        // livestream (the view count is returned and not the count of people watching currently
        // the livestream)
        if (getStreamType() != StreamType.LIVE_STREAM) {
            try {
                return getViewCountFromAccessibilityData();
            } catch (final Exception ignored) {
            }
        }

        // Fallback to a short view count, always used for livestreams (see why above)
        if (videoInfo.has("videoInfo")) {
            // Returned in playlists, in the form: view count separator upload date
            try {
                return getViewCountFromViewCountText(videoInfo.getObject("videoInfo")
                        .getArray("runs")
                        .getObject(0)
                        .getString("text", ""), true);
            } catch (final Exception ignored) {
            }
        }

        if (videoInfo.has("shortViewCountText")) {
            // Returned everywhere but in playlists, used by the website to show view counts
            try {
                final String shortViewCountText =
                        getTextFromObject(videoInfo.getObject("shortViewCountText"))
                                .orElse(null);
                if (!isNullOrEmpty(shortViewCountText)) {
                    return getViewCountFromViewCountText(shortViewCountText, true);
                }
            } catch (final Exception ignored) {
            }
        }

        // No view count extracted: return -1, as the view count can be hidden by creators on videos
        return -1;
    }

    private long getViewCountFromViewCountText(@Nonnull final String viewCountText,
                                               final boolean isMixedNumber)
            throws NumberFormatException, ParsingException {
        // These approaches are language dependent
        if (viewCountText.toLowerCase().contains(NO_VIEWS_LOWERCASE)) {
            return 0;
        } else if (viewCountText.toLowerCase().contains("recommended")) {
            return -1;
        }

        return isMixedNumber ? Utils.mixedNumberWordToLong(viewCountText)
                : Long.parseLong(Utils.removeNonDigitCharacters(viewCountText));
    }

    private long getViewCountFromAccessibilityData()
            throws NumberFormatException, Parser.RegexException {
        // These approaches are language dependent
        final String videoInfoTitleAccessibilityData = videoInfo.getObject("title")
                .getObject("accessibility")
                .getObject("accessibilityData")
                .getString("label", "");

        if (videoInfoTitleAccessibilityData.toLowerCase().endsWith(NO_VIEWS_LOWERCASE)) {
            return 0;
        }

        return Long.parseLong(Utils.removeNonDigitCharacters(
                Parser.matchGroup1(ACCESSIBILITY_DATA_VIEW_COUNT_REGEX,
                        videoInfoTitleAccessibilityData)));
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        return getThumbnailsFromInfoItem(videoInfo);
    }

    private boolean isPremium() {
        final JsonArray badges = videoInfo.getArray("badges");
        for (final Object badge : badges) {
            if (((JsonObject) badge).getObject("metadataBadgeRenderer")
                    .getString("label", "").equals("Premium")) {
                return true;
            }
        }
        return false;
    }

    private boolean isPremiere() {
        if (isPremiere == null) {
            isPremiere = videoInfo.has("upcomingEventData");
        }
        return isPremiere;
    }

    private Instant getInstantFromPremiere() throws ParsingException {
        final JsonObject upcomingEventData = videoInfo.getObject("upcomingEventData");
        final String startTime = upcomingEventData.getString("startTime");

        try {
            return Instant.ofEpochSecond(Long.parseLong(startTime));
        } catch (final Exception e) {
            final String message = "Could not parse date from premiere: \"" + startTime + "\"";
            throw new ParsingException(message, e);
        }
    }

    @Nullable
    @Override
    public String getShortDescription() {
        return getTextFromObject(videoInfo.getArray("detailedMetadataSnippets")
                .getObject(0)
                .getObject("snippetText"))
                .or(() -> getTextFromObject(videoInfo.getObject("descriptionSnippet")))
                .orElse(null);
    }

    @Override
    public boolean isShortFormContent() throws ParsingException {
        try {
            final String webPageType = videoInfo.getObject("navigationEndpoint")
                    .getObject("commandMetadata").getObject("webCommandMetadata")
                    .getString("webPageType");

            boolean isShort = !isNullOrEmpty(webPageType)
                    && webPageType.equals("WEB_PAGE_TYPE_SHORTS");

            if (!isShort) {
                isShort = videoInfo.getObject("navigationEndpoint").has("reelWatchEndpoint");
            }

            if (!isShort) {
                final JsonObject thumbnailTimeOverlay = videoInfo.getArray("thumbnailOverlays")
                        .stream()
                        .filter(JsonObject.class::isInstance)
                        .map(JsonObject.class::cast)
                        .filter(thumbnailOverlay -> thumbnailOverlay.has(
                                "thumbnailOverlayTimeStatusRenderer"))
                        .map(thumbnailOverlay -> thumbnailOverlay.getObject(
                                "thumbnailOverlayTimeStatusRenderer"))
                        .findFirst()
                        .orElse(null);

                if (!isNullOrEmpty(thumbnailTimeOverlay)) {
                    isShort = thumbnailTimeOverlay.getString("style", "")
                            .equalsIgnoreCase("SHORTS")
                            || thumbnailTimeOverlay.getObject("icon")
                            .getString("iconType", "")
                            .toLowerCase()
                            .contains("shorts");
                }
            }

            return isShort;
        } catch (final Exception e) {
            throw new ParsingException("Could not determine if this is short-form content", e);
        }
    }

    private boolean isMembersOnly() throws ParsingException {
        return videoInfo.getArray("badges")
            .stream()
            .filter(JsonObject.class::isInstance)
            .map(JsonObject.class::cast)
            .map(badge -> badge.getObject("metadataBadgeRenderer").getString("style"))
            .anyMatch("BADGE_STYLE_TYPE_MEMBERS_ONLY"::equals);
    }


    @Nonnull
    @Override
    public ContentAvailability getContentAvailability() throws ParsingException {
        if (isPremiere()) {
            return ContentAvailability.UPCOMING;
        }

        if (isMembersOnly()) {
            return ContentAvailability.MEMBERSHIP;
        }

        if (isPremium()) {
            return ContentAvailability.PAID;
        }

        return ContentAvailability.AVAILABLE;
    }

}
