package org.schabi.newpipe.extractor.services.youtube.invidious;

import static org.schabi.newpipe.extractor.utils.Utils.HTTP;
import static org.schabi.newpipe.extractor.utils.Utils.HTTPS;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.localization.DateWrapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import javax.annotation.Nonnull;

public final class InvidiousParsingHelper {
    private InvidiousParsingHelper() {
        // No impl pls
    }

    /**
     * Get valid JsonObject
     * <p>
     * Checks status code and handle JSON parsing
     *
     * @param response the response got from the service
     * @param apiUrl   the url used to call the service
     * @return the JsonObject
     * @throws ExtractionException if the HTTP code indicate an error or the json parsing went wrong
     */
    public static JsonObject getValidJsonObjectFromResponse(
            final Response response,
            final String apiUrl
    ) throws ExtractionException {
        final String responseBody = getValidResponseBody(response, apiUrl);

        try {
            return JsonParser.object().from(responseBody);
        } catch (final JsonParserException e) {
            throw new ExtractionException("Could not parse json from page \"" + apiUrl + "\"", e);
        }
    }

    /**
     * Get valid Response body
     * <p>
     * Checks status code and handle JSON parsing
     *
     * @param response the response got from the service
     * @param apiUrl   the url used to call the service
     * @return the response body
     * @throws ExtractionException if the HTTP code indicate an error or the json parsing went wrong
     */
    public static String getValidResponseBody(
            final Response response,
            final String apiUrl
    ) throws ExtractionException {
        if (response.responseCode() == 404) {
            throw new ContentNotAvailableException("Could not get page " + apiUrl
                    + " (HTTP " + response.responseCode() + " : " + response.responseMessage());
        } else if (response.responseCode() >= 400) {
            throw new ExtractionException("Could not get page " + apiUrl
                    + " (HTTP " + response.responseCode() + " : " + response.responseMessage());
        }

        return response.responseBody();
    }

    /**
     * Get valid Response body
     * <p>
     * Checks status code and handle JSON parsing
     *
     * @param response the response got from the service
     * @param apiUrl   the url used to call the service
     * @return the JsonArray
     * @throws ExtractionException if the HTTP code indicate an error or the json parsing went wrong
     */
    public static JsonArray getValidJsonArrayFromResponse(
            final Response response,
            final String apiUrl
    ) throws ExtractionException {
        final String responseBody = getValidResponseBody(response, apiUrl);

        try {
            return JsonParser.array().from(responseBody);
        } catch (final JsonParserException e) {
            throw new ExtractionException("Could not parse json from page \"" + apiUrl + "\"", e);
        }
    }

    public static DateWrapper getUploadDateFromEpochTime(final long epochTime) {
        return new DateWrapper(
                OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneOffset.UTC));
    }

    public static String getUid(@Nonnull final String id) {
        if (id.startsWith("user/")) {
            return id.substring(5);
        } else if (id.startsWith("channel/")) {
            return id.substring(8);
        }

        return id;
    }

    public static Page getPage(final String url, final int page) {
        return new Page(url + "?page=" + page, String.valueOf(page));
    }

    /**
     * Get thumbnail URL at a reasonable quality
     *
     * @param thumbnails an array of thumbnails
     * @return a thumbnail URL at a reasonable quality
     */
    public static String getThumbnailUrl(final JsonArray thumbnails) {
        String url = "";

        if (thumbnails.isEmpty()) {
            return url;
        } else if (thumbnails.size() == 1) {
            url = thumbnails.getObject(0).getString("url");
        } else {
            url = thumbnails.getObject(thumbnails.size() - 1).getString("url");
            for (int i = 1; i < thumbnails.size(); i++) {
                final JsonObject thumbnail = thumbnails.getObject(i);
                final String quality = thumbnail.getString("quality");
                if ("high".equals(quality)) {
                    url = thumbnail.getString("url");
                    break;
                }
            }
        }
        return fixThumbnailUrl(url);
    }

    public static String fixThumbnailUrl(final String thumbnailUrl) {
        if (thumbnailUrl.startsWith(HTTP) || thumbnailUrl.startsWith(HTTPS)) {
            return thumbnailUrl;
        }

        if (thumbnailUrl.startsWith("//")) {
            return HTTPS + thumbnailUrl.substring(2);
        }

        return HTTPS + thumbnailUrl;
    }
}
