// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.Image.ResolutionLevel;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.utils.ImageSuffix;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.Image.HEIGHT_UNKNOWN;
import static org.schabi.newpipe.extractor.Image.WIDTH_UNKNOWN;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;
import static org.schabi.newpipe.extractor.utils.Utils.replaceHttpWithHttps;

public final class BandcampExtractorHelper {

    /**
     * List of image IDs which preserve aspect ratio with their theoretical dimension known.
     *
     * <p>
     * Bandcamp images are not always squares, so images which preserve aspect ratio are only used.
     * </p>
     *
     * <p>
     * One of the direct consequences of this specificity is that only one dimension of images is
     * known at time, depending of the image ID.
     * </p>
     *
     * <p>
     * Note also that dimensions are only theoretical because if the image size is less than the
     * dimensions of the image ID, it will be not upscaled but kept to its original size.
     * </p>
     *
     * <p>
     * IDs come from <a href="https://gist.github.com/f2k1de/06f5fd0ae9c919a7c3693a44ee522213">the
     * GitHub Gist "Bandcamp File Format Parameters" by f2k1de</a>
     * </p>
     */
    private static final List<ImageSuffix> IMAGE_URL_SUFFIXES_AND_RESOLUTIONS = List.of(
            // ID | HEIGHT | WIDTH
            new ImageSuffix("10.jpg", HEIGHT_UNKNOWN, 1200, ResolutionLevel.HIGH),
            new ImageSuffix("101.jpg", 90, WIDTH_UNKNOWN, ResolutionLevel.LOW),
            new ImageSuffix("170.jpg", 422, WIDTH_UNKNOWN, ResolutionLevel.MEDIUM),
            // 180 returns the same image aspect ratio and size as 171
            new ImageSuffix("171.jpg", 646, WIDTH_UNKNOWN, ResolutionLevel.MEDIUM),
            new ImageSuffix("20.jpg", HEIGHT_UNKNOWN, 1024, ResolutionLevel.HIGH),
            // 203 returns the same image aspect ratio and size as 200
            new ImageSuffix("200.jpg", 420, WIDTH_UNKNOWN, ResolutionLevel.MEDIUM),
            new ImageSuffix("201.jpg", 280, WIDTH_UNKNOWN, ResolutionLevel.MEDIUM),
            new ImageSuffix("202.jpg", 140, WIDTH_UNKNOWN, ResolutionLevel.LOW),
            new ImageSuffix("204.jpg", 360, WIDTH_UNKNOWN, ResolutionLevel.MEDIUM),
            new ImageSuffix("205.jpg", 240, WIDTH_UNKNOWN, ResolutionLevel.MEDIUM),
            new ImageSuffix("206.jpg", 180, WIDTH_UNKNOWN, ResolutionLevel.MEDIUM),
            new ImageSuffix("207.jpg", 120, WIDTH_UNKNOWN, ResolutionLevel.LOW),
            new ImageSuffix("43.jpg", 100, WIDTH_UNKNOWN, ResolutionLevel.LOW),
            new ImageSuffix("44.jpg", 200, WIDTH_UNKNOWN, ResolutionLevel.MEDIUM));

    private static final String IMAGE_URL_APPENDIX_AND_EXTENSION_REGEX = "_\\d+\\.\\w+";
    private static final String IMAGES_DOMAIN_AND_PATH = "https://f4.bcbits.com/img/";

    public static final String BASE_URL = "https://bandcamp.com";
    public static final String BASE_API_URL = BASE_URL + "/api";

    private BandcampExtractorHelper() {
    }

    /**
     * Translate all these parameters together to the URL of the corresponding album or track
     * using the mobile API
     */
    public static String getStreamUrlFromIds(final long bandId,
                                             final long itemId,
                                             final String itemType) throws ParsingException {
        try {
            final String jsonString = NewPipe.getDownloader().get(
                    BASE_API_URL + "/mobile/22/tralbum_details?band_id=" + bandId
                            + "&tralbum_id=" + itemId + "&tralbum_type=" + itemType.charAt(0))
                    .responseBody();

            return replaceHttpWithHttps(JsonParser.object().from(jsonString)
                    .getString("bandcamp_url"));

        } catch (final JsonParserException | ReCaptchaException | IOException e) {
            throw new ParsingException("Ids could not be translated to URL", e);
        }

    }

    /**
     * Fetch artist details from mobile endpoint.
     * <a href="https://notabug.org/fynngodau/bandcampDirect/wiki/
     * rewindBandcamp+%E2%80%93+Fetching+artist+details">
     * More technical info.</a>
     */
    public static JsonObject getArtistDetails(final String id) throws ParsingException {
        try {
            return JsonParser.object().from(NewPipe.getDownloader().postWithContentTypeJson(
                    BASE_API_URL + "/mobile/22/band_details",
                    Collections.emptyMap(),
                    JsonWriter.string()
                            .object()
                            .value("band_id", id)
                            .end()
                            .done()
                            .getBytes(StandardCharsets.UTF_8)).responseBody());
        } catch (final IOException | ReCaptchaException | JsonParserException e) {
            throw new ParsingException("Could not download band details", e);
        }
    }

    /**
     * Generate an image url from an image ID.
     *
     * <p>
     * The image ID {@code 10} was chosen because it provides images wide up to 1200px (when
     * the original image width is more than or equal this resolution).
     * </p>
     *
     * <p>
     * Other integer values are possible as well (e.g. 0 is a very large resolution, possibly the
     * original); see {@link #IMAGE_URL_SUFFIXES_AND_RESOLUTIONS} for more details about image
     * resolution IDs.
     * </p>
     *
     * @param id      the image ID
     * @param isAlbum whether the image is the cover of an album or a track
     * @return a URL of the image with this ID with a width up to 1200px
     */
    @Nonnull
    public static String getImageUrl(final long id, final boolean isAlbum) {
        return IMAGES_DOMAIN_AND_PATH + (isAlbum ? 'a' : "") + id + "_10.jpg";
    }

    /**
     * @return <code>true</code> if the given URL looks like it comes from a bandcamp custom domain
     * or if it comes from <code>bandcamp.com</code> itself
     */
    public static boolean isSupportedDomain(final String url) throws ParsingException {

        // Accept all bandcamp.com URLs
        if (url.toLowerCase().matches("https?://.+\\.bandcamp\\.com(/.*)?")) {
            return true;
        }

        try {
            // Test other URLs for whether they contain a footer that links to bandcamp
            return Jsoup.parse(NewPipe.getDownloader().get(url).responseBody())
                    .getElementById("pgFt")
                    .getElementById("pgFt-inner")
                    .getElementById("footer-logo-wrapper")
                    .getElementById("footer-logo")
                    .getElementsByClass("hiddenAccess")
                    .text().equals("Bandcamp");
        } catch (final NullPointerException e) {
            return false;
        } catch (final IOException | ReCaptchaException e) {
            throw new ParsingException("Could not determine whether URL is custom domain "
                    + "(not available? network error?)");
        }
    }

    /**
     * Whether the URL points to a radio kiosk.
     * @param url the URL to check
     * @return true if the URL matches {@code https://bandcamp.com/?show=SHOW_ID}
     */
    public static boolean isRadioUrl(final String url) {
        return url.toLowerCase().matches("https?://bandcamp\\.com/\\?show=\\d+");
    }

    public static DateWrapper parseDate(final String textDate) throws ParsingException {
        try {
            final ZonedDateTime zonedDateTime = ZonedDateTime.parse(textDate,
                    DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH));
            return new DateWrapper(zonedDateTime.toOffsetDateTime(), false);
        } catch (final DateTimeException e) {
            throw new ParsingException("Could not parse date '" + textDate + "'", e);
        }
    }

    /**
     * Get a list of images from a search result {@link Element}.
     *
     * <p>
     * This method will call {@link #getImagesFromImageUrl(String)} using the first non null and
     * non empty image URL found from the {@code src} attribute of {@code img} HTML elements, or an
     * empty string if no valid image URL was found.
     * </p>
     *
     * @param searchResult a search result {@link Element}
     * @return an unmodifiable list of {@link Image}s, which is never null but can be empty, in the
     * case where no valid image URL was found
     */
    @Nonnull
    public static List<Image> getImagesFromSearchResult(@Nonnull final Element searchResult) {
        return getImagesFromImageUrl(searchResult.getElementsByClass("art")
                .stream()
                .flatMap(element -> element.getElementsByTag("img").stream())
                .map(element -> element.attr("src"))
                .filter(imageUrl -> !isNullOrEmpty(imageUrl))
                .findFirst()
                .orElse(""));
    }

    /**
     * Get all images which have resolutions preserving aspect ratio from an image URL.
     *
     * <p>
     * This method will remove the image ID and its extension from the end of the URL and then call
     * {@link #getImagesFromImageBaseUrl(String)}.
     * </p>
     *
     * @param imageUrl the full URL of an image provided by Bandcamp, such as in its HTML code
     * @return an unmodifiable list of {@link Image}s, which is never null but can be empty, in the
     * case where the image URL has been not extracted (and so is null or empty)
     */
    @Nonnull
    public static List<Image> getImagesFromImageUrl(@Nullable final String imageUrl) {
        if (isNullOrEmpty(imageUrl)) {
            return List.of();
        }

        return getImagesFromImageBaseUrl(
                imageUrl.replaceFirst(IMAGE_URL_APPENDIX_AND_EXTENSION_REGEX, "_"));
    }

    /**
     * Get all images which have resolutions preserving aspect ratio from an image ID.
     *
     * <p>
     * This method will call {@link #getImagesFromImageBaseUrl(String)}.
     * </p>
     *
     * @param id      the id of an image provided by Bandcamp
     * @param isAlbum whether the image is the cover of an album
     * @return an unmodifiable list of {@link Image}s, which is never null but can be empty, in the
     * case where the image ID has been not extracted (and so equal to 0)
     */
    @Nonnull
    public static List<Image> getImagesFromImageId(final long id, final boolean isAlbum) {
        if (id == 0) {
            return List.of();
        }

        return getImagesFromImageBaseUrl(IMAGES_DOMAIN_AND_PATH + (isAlbum ? 'a' : "") + id + "_");
    }

    /**
     * Get all images resolutions preserving aspect ratio from a base image URL.
     *
     * <p>
     * Base image URLs are images containing the image path, a {@code a} letter if it comes from an
     * album, its ID and an underscore.
     * </p>
     *
     * <p>
     * Images resolutions returned are the ones of {@link #IMAGE_URL_SUFFIXES_AND_RESOLUTIONS}.
     * </p>
     *
     * @param baseUrl the base URL of the image
     * @return an unmodifiable and non-empty list of {@link Image}s
     */
    @Nonnull
    private static List<Image> getImagesFromImageBaseUrl(@Nonnull final String baseUrl) {
        return IMAGE_URL_SUFFIXES_AND_RESOLUTIONS.stream()
                .map(imageSuffix -> new Image(baseUrl + imageSuffix.getSuffix(),
                        imageSuffix.getHeight(), imageSuffix.getWidth(),
                        imageSuffix.getResolutionLevel()))
                .collect(Collectors.toUnmodifiableList());
    }
}
