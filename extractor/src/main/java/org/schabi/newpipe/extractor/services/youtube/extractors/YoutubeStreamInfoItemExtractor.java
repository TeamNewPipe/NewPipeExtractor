package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getThumbnailUrlFromInfoItem;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/*
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * YoutubeStreamInfoItemExtractor.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

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
        final String name = getTextFromObject(videoInfo.getObject("title"));
        if (!isNullOrEmpty(name)) {
            return name;
        }
        throw new ParsingException("Could not get name");
    }

    @Override
    public long getDuration() throws ParsingException {
        if (getStreamType() == StreamType.LIVE_STREAM) {
            return -1;
        }

        String duration = getTextFromObject(videoInfo.getObject("lengthText"));

        if (isNullOrEmpty(duration)) {
            // Available in playlists for videos
            duration = videoInfo.getString("lengthSeconds");

            if (isNullOrEmpty(duration)) {
                final JsonObject timeOverlay = videoInfo.getArray("thumbnailOverlays")
                        .stream()
                        .filter(JsonObject.class::isInstance)
                        .map(JsonObject.class::cast)
                        .filter(thumbnailOverlay ->
                                thumbnailOverlay.has("thumbnailOverlayTimeStatusRenderer"))
                        .findFirst()
                        .orElse(null);

                if (timeOverlay != null) {
                    duration = getTextFromObject(
                            timeOverlay.getObject("thumbnailOverlayTimeStatusRenderer")
                                    .getObject("text"));
                }
            }

            if (isNullOrEmpty(duration)) {
                if (isPremiere()) {
                    // Premieres can be livestreams, so the duration is not available in this
                    // case
                    return -1;
                }

                throw new ParsingException("Could not get duration");
            }
        }

        return YoutubeParsingHelper.parseDurationString(duration);
    }

    @Override
    public String getUploaderName() throws ParsingException {
        String name = getTextFromObject(videoInfo.getObject("longBylineText"));

        if (isNullOrEmpty(name)) {
            name = getTextFromObject(videoInfo.getObject("ownerText"));

            if (isNullOrEmpty(name)) {
                name = getTextFromObject(videoInfo.getObject("shortBylineText"));

                if (isNullOrEmpty(name)) {
                    throw new ParsingException("Could not get uploader name");
                }
            }
        }

        return name;
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        String url = getUrlFromNavigationEndpoint(videoInfo.getObject("longBylineText")
                .getArray("runs").getObject(0).getObject("navigationEndpoint"));

        if (isNullOrEmpty(url)) {
            url = getUrlFromNavigationEndpoint(videoInfo.getObject("ownerText")
                    .getArray("runs").getObject(0).getObject("navigationEndpoint"));

            if (isNullOrEmpty(url)) {
                url = getUrlFromNavigationEndpoint(videoInfo.getObject("shortBylineText")
                        .getArray("runs").getObject(0).getObject("navigationEndpoint"));

                if (isNullOrEmpty(url)) {
                    throw new ParsingException("Could not get uploader url");
                }
            }
        }

        return url;
    }

    @Nullable
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        if (videoInfo.has("channelThumbnailSupportedRenderers")) {
            return JsonUtils.getArray(videoInfo, "channelThumbnailSupportedRenderers"
                    + ".channelThumbnailWithLinkRenderer.thumbnail.thumbnails")
                    .getObject(0).getString("url");
        }

        if (videoInfo.has("channelThumbnail")) {
            return JsonUtils.getArray(videoInfo, "channelThumbnail.thumbnails")
                    .getObject(0).getString("url");
        }

        return null;
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return YoutubeParsingHelper.isVerified(videoInfo.getArray("ownerBadges"));
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        if (getStreamType().equals(StreamType.LIVE_STREAM)) {
            return null;
        }

        if (isPremiere()) {
            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(getDateFromPremiere());
        }

        String publishedTimeText = getTextFromObject(videoInfo.getObject("publishedTimeText"));

        if (isNullOrEmpty(publishedTimeText) && videoInfo.has("videoInfo")) {
            /*
            Returned in playlists, in the form: view count separator upload date
            */
            publishedTimeText = videoInfo.getObject("videoInfo")
                    .getArray("runs")
                    .getObject(2)
                    .getString("text");
        }

        return isNullOrEmpty(publishedTimeText) ? null : publishedTimeText;
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        if (getStreamType().equals(StreamType.LIVE_STREAM)) {
            return null;
        }

        if (isPremiere()) {
            return new DateWrapper(getDateFromPremiere());
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

        final String viewCountText = getTextFromObject(videoInfo.getObject("viewCountText"));
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
                        getTextFromObject(videoInfo.getObject("shortViewCountText"));
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

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return getThumbnailUrlFromInfoItem(videoInfo);
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

    private OffsetDateTime getDateFromPremiere() throws ParsingException {
        final JsonObject upcomingEventData = videoInfo.getObject("upcomingEventData");
        final String startTime = upcomingEventData.getString("startTime");

        try {
            return OffsetDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(startTime)),
                    ZoneOffset.UTC);
        } catch (final Exception e) {
            throw new ParsingException("Could not parse date from premiere: \"" + startTime + "\"");
        }
    }

    @Nullable
    @Override
    public String getShortDescription() throws ParsingException {

        if (videoInfo.has("detailedMetadataSnippets")) {
            return getTextFromObject(videoInfo.getArray("detailedMetadataSnippets")
                    .getObject(0).getObject("snippetText"));
        }

        if (videoInfo.has("descriptionSnippet")) {
            return getTextFromObject(videoInfo.getObject("descriptionSnippet"));
        }

        return null;
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
}
