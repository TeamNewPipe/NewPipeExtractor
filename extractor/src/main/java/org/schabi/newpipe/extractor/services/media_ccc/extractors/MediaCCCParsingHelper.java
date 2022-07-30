package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.Image.ResolutionLevel;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.localization.Localization;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.schabi.newpipe.extractor.Image.HEIGHT_UNKNOWN;
import static org.schabi.newpipe.extractor.Image.WIDTH_UNKNOWN;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public final class MediaCCCParsingHelper {
    // conference_slug/room_slug
    private static final Pattern LIVE_STREAM_ID_PATTERN = Pattern.compile("\\w+/\\w+");
    private static JsonArray liveStreams = null;

    private MediaCCCParsingHelper() { }

    public static OffsetDateTime parseDateFrom(final String textualUploadDate)
            throws ParsingException {
        try {
            return OffsetDateTime.parse(textualUploadDate);
        } catch (final DateTimeParseException e) {
            throw new ParsingException("Could not parse date: \"" + textualUploadDate + "\"", e);
        }
    }

    /**
     * Check whether an id is a live stream id
     * @param id the {@code id} to check
     * @return returns {@code true} if the {@code id} is formatted like
     *         {@code {conference_slug}/{room_slug}}; {@code false} otherwise
     */
    public static boolean isLiveStreamId(final String id) {
        return LIVE_STREAM_ID_PATTERN.matcher(id).find();
    }

    /**
     * Get currently available live streams from
     * <a href="https://streaming.media.ccc.de/streams/v2.json">
     *     https://streaming.media.ccc.de/streams/v2.json</a>.
     * Use this method to cache requests, because they can get quite big.
     * TODO: implement better caching policy (max-age: 3 min)
     * @param downloader The downloader to use for making the request
     * @param localization The localization to be used. Will most likely be ignored.
     * @return {@link JsonArray} containing current conferences and info about their rooms and
     *         streams.
     * @throws ExtractionException if the data could not be fetched or the retrieved data could not
     *                             be parsed to a {@link JsonArray}
     */
    public static JsonArray getLiveStreams(final Downloader downloader,
                                           final Localization localization)
            throws ExtractionException {
        if (liveStreams == null) {
            try {
                final String site = downloader.get("https://streaming.media.ccc.de/streams/v2.json",
                        localization).responseBody();
                liveStreams = JsonParser.array().from(site);
            } catch (final IOException | ReCaptchaException e) {
                throw new ExtractionException("Could not get live stream JSON.", e);
            } catch (final JsonParserException e) {
                throw new ExtractionException("Could not parse JSON.", e);
            }
        }
        return liveStreams;
    }

    /**
     * Get an {@link Image} list from a given image logo URL.
     *
     * <p>
     * If the image URL is null or empty, an empty list is returned; otherwise, a singleton list is
     * returned containing an {@link Image} with the image URL with its height, width and
     * resolution unknown.
     * </p>
     *
     * @param logoImageUrl a logo image URL, which can be null or empty
     * @return an unmodifiable list of {@link Image}s, which is always empty or a singleton
     */
    @Nonnull
    public static List<Image> getImageListFromLogoImageUrl(@Nullable final String logoImageUrl) {
        if (isNullOrEmpty(logoImageUrl)) {
            return List.of();
        }

        return List.of(new Image(logoImageUrl, HEIGHT_UNKNOWN, WIDTH_UNKNOWN,
                ResolutionLevel.UNKNOWN));
    }

    /**
     * Get the {@link Image} list of thumbnails from a given stream item.
     *
     * <p>
     * MediaCCC API provides two thumbnails for a stream item: a {@code thumb_url} one, which is
     * medium quality and a {@code poster_url} one, which is high quality in most cases.
     * </p>
     *
     * @param streamItem a stream JSON item of MediaCCC's API, which must not be null
     * @return an unmodifiable list, which is never null but can be empty.
     */
    @Nonnull
    public static List<Image> getThumbnailsFromStreamItem(@Nonnull final JsonObject streamItem) {
        return getThumbnailsFromObject(streamItem, "thumb_url", "poster_url");
    }

    /**
     * Get the {@link Image} list of thumbnails from a given live stream item.
     *
     * <p>
     * MediaCCC API provides two URL thumbnails for a livestream item: a {@code thumb} one,
     * which should be medium quality and a {@code poster_url} one, which should be high quality.
     * </p>
     *
     * @param liveStreamItem a stream JSON item of MediaCCC's API, which must not be null
     * @return an unmodifiable list, which is never null but can be empty.
     */
    @Nonnull
    public static List<Image> getThumbnailsFromLiveStreamItem(
            @Nonnull final JsonObject liveStreamItem) {
        return getThumbnailsFromObject(liveStreamItem, "thumb", "poster");
    }

    /**
     * Utility method to get an {@link Image} list of thumbnails from a stream or a livestream.
     *
     * <p>
     * MediaCCC's API thumbnails come from two elements: a {@code thumb} element, which links to a
     * medium thumbnail and a {@code poster} element, which links to a high thumbnail.
     * </p>
     * <p>
     * Thumbnails are only added if their URLs are not null or empty.
     * </p>
     *
     * @param streamOrLivestreamItem a (live)stream JSON item of MediaCCC's API, which must not be
     *                               null
     * @param thumbUrlKey  the name of the {@code thumb} URL key
     * @param posterUrlKey the name of the {@code poster} URL key
     * @return an unmodifiable list, which is never null but can be empty.
     */
    @Nonnull
    private static List<Image> getThumbnailsFromObject(
            @Nonnull final JsonObject streamOrLivestreamItem,
            @Nonnull final String thumbUrlKey,
            @Nonnull final String posterUrlKey) {
        final List<Image> imageList = new ArrayList<>(2);

        final String thumbUrl = streamOrLivestreamItem.getString(thumbUrlKey);
        if (!isNullOrEmpty(thumbUrl)) {
            imageList.add(new Image(thumbUrl, HEIGHT_UNKNOWN, WIDTH_UNKNOWN,
                    ResolutionLevel.MEDIUM));
        }

        final String posterUrl = streamOrLivestreamItem.getString(posterUrlKey);
        if (!isNullOrEmpty(posterUrl)) {
            imageList.add(new Image(posterUrl, HEIGHT_UNKNOWN, WIDTH_UNKNOWN,
                    ResolutionLevel.HIGH));
        }

        return Collections.unmodifiableList(imageList);
    }
}
