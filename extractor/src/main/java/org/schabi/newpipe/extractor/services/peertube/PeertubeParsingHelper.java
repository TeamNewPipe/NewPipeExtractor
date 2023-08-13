package org.schabi.newpipe.extractor.services.peertube;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.Image.ResolutionLevel;
import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubePlaylistInfoItemExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeSepiaStreamInfoItemExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeStreamInfoItemExtractor;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Parser;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.schabi.newpipe.extractor.Image.HEIGHT_UNKNOWN;
import static org.schabi.newpipe.extractor.Image.WIDTH_UNKNOWN;
import static org.schabi.newpipe.extractor.utils.Utils.isBlank;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public final class PeertubeParsingHelper {
    public static final String START_KEY = "start";
    public static final String COUNT_KEY = "count";
    public static final int ITEMS_PER_PAGE = 12;
    public static final String START_PATTERN = "start=(\\d*)";

    private PeertubeParsingHelper() {
    }

    public static void validate(final JsonObject json) throws ContentNotAvailableException {
        final String error = json.getString("error");
        if (!isBlank(error)) {
            throw new ContentNotAvailableException(error);
        }
    }

    public static OffsetDateTime parseDateFrom(final String textualUploadDate)
            throws ParsingException {
        try {
            return OffsetDateTime.ofInstant(Instant.parse(textualUploadDate), ZoneOffset.UTC);
        } catch (final DateTimeParseException e) {
            throw new ParsingException("Could not parse date: \"" + textualUploadDate + "\"", e);
        }
    }

    public static Page getNextPage(final String prevPageUrl, final long total) {
        final String prevStart;
        try {
            prevStart = Parser.matchGroup1(START_PATTERN, prevPageUrl);
        } catch (final Parser.RegexException e) {
            return null;
        }
        if (isBlank(prevStart)) {
            return null;
        }

        final long nextStart;
        try {
            nextStart = Long.parseLong(prevStart) + ITEMS_PER_PAGE;
        } catch (final NumberFormatException e) {
            return null;
        }

        if (nextStart >= total) {
            return null;
        } else {
            return new Page(prevPageUrl.replace(
                    START_KEY + "=" + prevStart, START_KEY + "=" + nextStart));
        }
    }

    public static void collectItemsFrom(final InfoItemsCollector collector,
                                        final JsonObject json,
                                        final String baseUrl) throws ParsingException {
        collectItemsFrom(collector, json, baseUrl, false);
    }

    /**
     * Collect items from the given JSON object with the given collector.
     *
     * <p>
     * Supported info item types are streams with their Sepia variant, channels and playlists.
     * </p>
     *
     * @param collector the collector used to collect information
     * @param json      the JSOn response to retrieve data from
     * @param baseUrl   the base URL of the instance
     * @param sepia     if we should use {@code PeertubeSepiaStreamInfoItemExtractor} to extract
     *                  streams or {@code PeertubeStreamInfoItemExtractor} otherwise
     */
    public static void collectItemsFrom(final InfoItemsCollector collector,
                                        final JsonObject json,
                                        final String baseUrl,
                                        final boolean sepia) throws ParsingException {
        final JsonArray contents;
        try {
            contents = (JsonArray) JsonUtils.getValue(json, "data");
        } catch (final Exception e) {
            throw new ParsingException("Unable to extract list info", e);
        }

        for (final Object c : contents) {
            if (c instanceof JsonObject) {
                JsonObject item = (JsonObject) c;

                // PeerTube playlists have the stream info encapsulated in an "video" object
                if (item.has("video")) {
                    item = item.getObject("video");
                }
                final boolean isPlaylistInfoItem = item.has("videosLength");
                final boolean isChannelInfoItem = item.has("followersCount");

                final InfoItemExtractor extractor;
                if (sepia) {
                    extractor = new PeertubeSepiaStreamInfoItemExtractor(item, baseUrl);
                } else if (isPlaylistInfoItem) {
                    extractor = new PeertubePlaylistInfoItemExtractor(item, baseUrl);
                } else if (isChannelInfoItem) {
                    extractor = new PeertubeChannelInfoItemExtractor(item, baseUrl);
                } else {
                    extractor = new PeertubeStreamInfoItemExtractor(item, baseUrl);
                }
                collector.commit(extractor);
            }
        }
    }

    /**
     * Get avatars from a {@code ownerAccount} or a {@code videoChannel} {@link JsonObject}.
     *
     * <p>
     * If the {@code avatars} {@link JsonArray} is present and non null or empty, avatars will be
     * extracted from this array using {@link #getImagesFromAvatarOrBannerArray(String, JsonArray)}.
     * </p>
     *
     * <p>
     * If that's not the case, an avatar will extracted using the {@code avatar} {@link JsonObject}.
     * </p>
     *
     * <p>
     * Note that only images for which paths are not null and not empty will be added to the
     * unmodifiable {@link Image} list returned.
     * </p>
     *
     * @param baseUrl                          the base URL of the PeerTube instance
     * @param ownerAccountOrVideoChannelObject the {@code ownerAccount} or {@code videoChannel}
     *                                         {@link JsonObject}
     * @return an unmodifiable list of {@link Image}s, which may be empty but never null
     */
    @Nonnull
    public static List<Image> getAvatarsFromOwnerAccountOrVideoChannelObject(
            @Nonnull final String baseUrl,
            @Nonnull final JsonObject ownerAccountOrVideoChannelObject) {
        return getImagesFromAvatarsOrBanners(baseUrl, ownerAccountOrVideoChannelObject,
                "avatars", "avatar");
    }

    /**
     * Get banners from a {@code ownerAccount} or a {@code videoChannel} {@link JsonObject}.
     *
     * <p>
     * If the {@code banners} {@link JsonArray} is present and non null or empty, banners will be
     * extracted from this array using {@link #getImagesFromAvatarOrBannerArray(String, JsonArray)}.
     * </p>
     *
     * <p>
     * If that's not the case, a banner will extracted using the {@code banner} {@link JsonObject}.
     * </p>
     *
     * <p>
     * Note that only images for which paths are not null and not empty will be added to the
     * unmodifiable {@link Image} list returned.
     * </p>
     *
     * @param baseUrl                          the base URL of the PeerTube instance
     * @param ownerAccountOrVideoChannelObject the {@code ownerAccount} or {@code videoChannel}
     *                                         {@link JsonObject}
     * @return an unmodifiable list of {@link Image}s, which may be empty but never null
     */
    @Nonnull
    public static List<Image> getBannersFromAccountOrVideoChannelObject(
            @Nonnull final String baseUrl,
            @Nonnull final JsonObject ownerAccountOrVideoChannelObject) {
        return getImagesFromAvatarsOrBanners(baseUrl, ownerAccountOrVideoChannelObject,
                "banners", "banner");
    }

    /**
     * Get thumbnails from a playlist or a video item {@link JsonObject}.
     *
     * <p>
     * PeerTube provides two thumbnails in its API: a low one, represented by the value of the
     * {@code thumbnailPath} key, and a medium one, represented by the value of the
     * {@code previewPath} key.
     * </p>
     *
     * <p>
     * If a value is not null or empty, an {@link Image} will be added to the list returned with
     * the URL to the thumbnail ({@code baseUrl + value}), a height and a width unknown and the
     * corresponding resolution level (see above).
     * </p>
     *
     * @param baseUrl                   the base URL of the PeerTube instance
     * @param playlistOrVideoItemObject the playlist or the video item {@link JsonObject}, which
     *                                  must not be null
     * @return an unmodifiable list of {@link Image}s, which is never null but can be empty
     */
    @Nonnull
    public static List<Image> getThumbnailsFromPlaylistOrVideoItem(
            @Nonnull final String baseUrl,
            @Nonnull final JsonObject playlistOrVideoItemObject) {
        final List<Image> imageList = new ArrayList<>(2);

        final String thumbnailPath = playlistOrVideoItemObject.getString("thumbnailPath");
        if (!isNullOrEmpty(thumbnailPath)) {
            imageList.add(new Image(baseUrl + thumbnailPath, HEIGHT_UNKNOWN, WIDTH_UNKNOWN,
                    ResolutionLevel.LOW));
        }

        final String previewPath = playlistOrVideoItemObject.getString("previewPath");
        if (!isNullOrEmpty(previewPath)) {
            imageList.add(new Image(baseUrl + previewPath, HEIGHT_UNKNOWN, WIDTH_UNKNOWN,
                    ResolutionLevel.MEDIUM));
        }

        return Collections.unmodifiableList(imageList);
    }

    /**
     * Utility method to get avatars and banners from video channels and accounts from given name
     * keys.
     *
     * <p>
     * Only images for which paths are not null and not empty will be added to the unmodifiable
     * {@link Image} list returned and only the width of avatars or banners is provided by the API,
     * and so is the only image dimension known.
     * </p>
     *
     * @param baseUrl                          the base URL of the PeerTube instance
     * @param ownerAccountOrVideoChannelObject the {@code ownerAccount} or {@code videoChannel}
     *                                         {@link JsonObject}
     * @param jsonArrayName                    the key name of the {@link JsonArray} to which
     *                                         extract all images available ({@code avatars} or
     *                                         {@code banners})
     * @param jsonObjectName                   the key name of the {@link JsonObject} to which
     *                                         extract a single image ({@code avatar} or
     *                                         {@code banner}), used as a fallback if the images
     *                                         {@link JsonArray} is null or empty
     * @return an unmodifiable list of {@link Image}s, which may be empty but never null
     */
    @Nonnull
    private static List<Image> getImagesFromAvatarsOrBanners(
            @Nonnull final String baseUrl,
            @Nonnull final JsonObject ownerAccountOrVideoChannelObject,
            @Nonnull final String jsonArrayName,
            @Nonnull final String jsonObjectName) {
        final JsonArray images = ownerAccountOrVideoChannelObject.getArray(jsonArrayName);

        if (!isNullOrEmpty(images))  {
            return getImagesFromAvatarOrBannerArray(baseUrl, images);
        }

        final JsonObject image = ownerAccountOrVideoChannelObject.getObject(jsonObjectName);
        final String path = image.getString("path");
        if (!isNullOrEmpty(path)) {
            return List.of(new Image(baseUrl + path, HEIGHT_UNKNOWN,
                    image.getInt("width", WIDTH_UNKNOWN), ResolutionLevel.UNKNOWN));
        }

        return List.of();
    }

    /**
     * Get {@link Image}s from an {@code avatars} or a {@code banners} {@link JsonArray}.
     *
     * <p>
     * Only images for which paths are not null and not empty will be added to the
     * unmodifiable {@link Image} list returned.
     * </p>
     *
     * <p>
     * Note that only the width of avatars or banners is provided by the API, and so only is the
     * only dimension known of images.
     * </p>
     *
     * @param baseUrl               the base URL of the PeerTube instance from which the
     *                              {@code avatarsOrBannersArray} {@link JsonArray} comes from
     * @param avatarsOrBannersArray an {@code avatars} or {@code banners} {@link JsonArray}
     * @return an unmodifiable list of {@link Image}s, which may be empty but never null
     */
    @Nonnull
    private static List<Image> getImagesFromAvatarOrBannerArray(
            @Nonnull final String baseUrl,
            @Nonnull final JsonArray avatarsOrBannersArray) {
        return avatarsOrBannersArray.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .filter(image -> !isNullOrEmpty(image.getString("path")))
                .map(image -> new Image(baseUrl + image.getString("path"), HEIGHT_UNKNOWN,
                        image.getInt("width", WIDTH_UNKNOWN), ResolutionLevel.UNKNOWN))
                .collect(Collectors.toUnmodifiableList());
    }
}
