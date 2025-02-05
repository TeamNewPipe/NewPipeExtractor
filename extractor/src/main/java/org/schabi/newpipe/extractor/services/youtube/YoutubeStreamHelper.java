package org.schabi.newpipe.extractor.services.youtube;

import com.grack.nanojson.JsonBuilder;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.schabi.newpipe.extractor.NewPipe.getDownloader;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.TVHTML5_CLIENT_ID;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.TVHTML5_CLIENT_VERSION;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.TVHTML5_USER_AGENT;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.WEB_EMBEDDED_CLIENT_ID;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.WEB_EMBEDDED_CLIENT_VERSION;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.CONTENT_CHECK_OK;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.CPN;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.RACY_CHECK_OK;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.VIDEO_ID;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_GAPIS_URL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.generateTParameter;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getAndroidUserAgent;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getClientHeaders;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getClientVersion;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getIosUserAgent;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getOriginReferrerHeaders;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getValidJsonResponseBody;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getYouTubeHeaders;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareJsonBuilder;

public final class YoutubeStreamHelper {

    private static final String PLAYER = "player";
    private static final String SERVICE_INTEGRITY_DIMENSIONS = "serviceIntegrityDimensions";
    private static final String PO_TOKEN = "poToken";
    private static final String BASE_YT_DESKTOP_WATCH_URL = "https://www.youtube.com/watch?v=";

    private YoutubeStreamHelper() {
    }

    @Nonnull
    public static JsonObject getWebMetadataPlayerResponse(
            @Nonnull final Localization localization,
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final String videoId) throws IOException, ExtractionException {
        final InnertubeClientRequestInfo innertubeClientRequestInfo =
                InnertubeClientRequestInfo.ofWebClient();
        innertubeClientRequestInfo.clientInfo.clientVersion = getClientVersion();

        final Map<String, List<String>> headers = getYouTubeHeaders();

        // We must always pass a valid visitorData to get valid player responses, which needs to be
        // got from YouTube
        innertubeClientRequestInfo.clientInfo.visitorData =
                YoutubeParsingHelper.getVisitorDataFromInnertube(innertubeClientRequestInfo,
                        localization, contentCountry, headers, YOUTUBEI_V1_URL, null, false);

        final JsonBuilder<JsonObject> builder = prepareJsonBuilder(localization, contentCountry,
                innertubeClientRequestInfo, null);

        addVideoIdCpnAndOkChecks(builder, videoId, null);

        final byte[] body = JsonWriter.string(builder.done())
                .getBytes(StandardCharsets.UTF_8);

        final String url = YOUTUBEI_V1_URL + PLAYER + "?" + DISABLE_PRETTY_PRINT_PARAMETER
                + "&$fields=microformat,playabilityStatus,storyboards,videoDetails";

        return JsonUtils.toJsonObject(getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(
                        url, headers, body, localization)));
    }

    @Nonnull
    public static JsonObject getTvHtml5PlayerResponse(
            @Nonnull final Localization localization,
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final String videoId,
            @Nonnull final String cpn,
            final int signatureTimestamp) throws IOException, ExtractionException {
        final InnertubeClientRequestInfo innertubeClientRequestInfo =
                InnertubeClientRequestInfo.ofTvHtml5Client();

        final Map<String, List<String>> headers = new HashMap<>(
                getClientHeaders(TVHTML5_CLIENT_ID, TVHTML5_CLIENT_VERSION));
        headers.putAll(getOriginReferrerHeaders("https://www.youtube.com"));
        headers.put("User-Agent", List.of(TVHTML5_USER_AGENT));

        // We must always pass a valid visitorData to get valid player responses, which needs to be
        // got from YouTube
        // For some reason, the TVHTML5 client doesn't support the visitor_id endpoint, use the
        // guide one instead, which is quite lightweight
        innertubeClientRequestInfo.clientInfo.visitorData =
                YoutubeParsingHelper.getVisitorDataFromInnertube(innertubeClientRequestInfo,
                        localization, contentCountry, headers, YOUTUBEI_V1_URL, null, true);

        final JsonBuilder<JsonObject> builder = prepareJsonBuilder(localization, contentCountry,
                innertubeClientRequestInfo, null);

        addVideoIdCpnAndOkChecks(builder, videoId, cpn);

        addPlaybackContext(builder, BASE_YT_DESKTOP_WATCH_URL + videoId, signatureTimestamp);

        final byte[] body = JsonWriter.string(builder.done())
                .getBytes(StandardCharsets.UTF_8);

        final String url = YOUTUBEI_V1_URL + PLAYER + "?" + DISABLE_PRETTY_PRINT_PARAMETER;

        return JsonUtils.toJsonObject(getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(url, headers, body, localization)));
    }

    @Nonnull
    public static JsonObject getWebFullPlayerResponse(
            @Nonnull final Localization localization,
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final String videoId,
            @Nonnull final String cpn,
            @Nonnull final PoTokenResult webPoTokenResult,
            final int signatureTimestamp) throws IOException, ExtractionException {
        final InnertubeClientRequestInfo innertubeClientRequestInfo =
                InnertubeClientRequestInfo.ofWebClient();
        innertubeClientRequestInfo.clientInfo.clientVersion = getClientVersion();
        innertubeClientRequestInfo.clientInfo.visitorData = webPoTokenResult.visitorData;

        final JsonBuilder<JsonObject> builder = prepareJsonBuilder(localization, contentCountry,
                innertubeClientRequestInfo, null);

        addVideoIdCpnAndOkChecks(builder, videoId, cpn);

        addPlaybackContext(builder, BASE_YT_DESKTOP_WATCH_URL + videoId, signatureTimestamp);

        addPoToken(builder, webPoTokenResult.playerRequestPoToken);

        final byte[] body = JsonWriter.string(builder.done())
                .getBytes(StandardCharsets.UTF_8);

        final String url = YOUTUBEI_V1_URL + PLAYER + "?" + DISABLE_PRETTY_PRINT_PARAMETER;

        return JsonUtils.toJsonObject(getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(
                        url, getYouTubeHeaders(), body, localization)));
    }

    @Nonnull
    public static JsonObject getWebEmbeddedPlayerResponse(
            @Nonnull final Localization localization,
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final String videoId,
            @Nonnull final String cpn,
            @Nullable final PoTokenResult webEmbeddedPoTokenResult,
            final int signatureTimestamp) throws IOException, ExtractionException {
        final InnertubeClientRequestInfo innertubeClientRequestInfo =
                InnertubeClientRequestInfo.ofWebEmbeddedPlayerClient();

        final Map<String, List<String>> headers = new HashMap<>(
                getClientHeaders(WEB_EMBEDDED_CLIENT_ID, WEB_EMBEDDED_CLIENT_VERSION));
        headers.putAll(getOriginReferrerHeaders("https://www.youtube.com"));

        final String embedUrl = BASE_YT_DESKTOP_WATCH_URL + videoId;

        // We must always pass a valid visitorData to get valid player responses, which needs to be
        // got from YouTube
        innertubeClientRequestInfo.clientInfo.visitorData = webEmbeddedPoTokenResult == null
                ? YoutubeParsingHelper.getVisitorDataFromInnertube(innertubeClientRequestInfo,
                        localization, contentCountry, headers, YOUTUBEI_V1_URL, embedUrl, false)
                : webEmbeddedPoTokenResult.visitorData;

        final JsonBuilder<JsonObject> builder = prepareJsonBuilder(localization, contentCountry,
                innertubeClientRequestInfo, embedUrl);

        addVideoIdCpnAndOkChecks(builder, videoId, cpn);

        addPlaybackContext(builder, embedUrl, signatureTimestamp);

        if (webEmbeddedPoTokenResult != null) {
            addPoToken(builder, webEmbeddedPoTokenResult.playerRequestPoToken);
        }

        final byte[] body = JsonWriter.string(builder.done())
                .getBytes(StandardCharsets.UTF_8);
        final String url = YOUTUBEI_V1_URL + PLAYER + "?" + DISABLE_PRETTY_PRINT_PARAMETER;

        return JsonUtils.toJsonObject(getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(url, headers, body, localization)));
    }

    public static JsonObject getAndroidPlayerResponse(
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final Localization localization,
            @Nonnull final String videoId,
            @Nonnull final String cpn,
            @Nonnull final PoTokenResult androidPoTokenResult)
            throws IOException, ExtractionException {
        final InnertubeClientRequestInfo innertubeClientRequestInfo =
                InnertubeClientRequestInfo.ofAndroidClient();
        innertubeClientRequestInfo.clientInfo.visitorData = androidPoTokenResult.visitorData;

        final Map<String, List<String>> headers =
                getMobileClientHeaders(getAndroidUserAgent(localization));

        final JsonBuilder<JsonObject> builder = prepareJsonBuilder(localization, contentCountry,
                innertubeClientRequestInfo, null);

        addVideoIdCpnAndOkChecks(builder, videoId, cpn);

        addPoToken(builder, androidPoTokenResult.playerRequestPoToken);

        final byte[] body = JsonWriter.string(builder.done())
                .getBytes(StandardCharsets.UTF_8);

        final String url = YOUTUBEI_V1_GAPIS_URL + PLAYER + "?" + DISABLE_PRETTY_PRINT_PARAMETER
                + "&t=" + generateTParameter() + "&id=" + videoId;

        return JsonUtils.toJsonObject(getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(url, headers, body, localization)));
    }

    public static JsonObject getAndroidReelPlayerResponse(
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final Localization localization,
            @Nonnull final String videoId,
            @Nonnull final String cpn) throws IOException, ExtractionException {
        final InnertubeClientRequestInfo innertubeClientRequestInfo =
                InnertubeClientRequestInfo.ofAndroidClient();

        final Map<String, List<String>> headers =
                getMobileClientHeaders(getAndroidUserAgent(localization));

        // We must always pass a valid visitorData to get valid player responses, which needs to be
        // got from YouTube
        innertubeClientRequestInfo.clientInfo.visitorData =
                YoutubeParsingHelper.getVisitorDataFromInnertube(innertubeClientRequestInfo,
                        localization, contentCountry, headers, YOUTUBEI_V1_GAPIS_URL, null, false);

        final JsonBuilder<JsonObject> builder = prepareJsonBuilder(localization, contentCountry,
                innertubeClientRequestInfo, null);

        addVideoIdCpnAndOkChecks(builder, videoId, cpn);

        builder.object("playerRequest")
                .value(VIDEO_ID, videoId)
                .end()
                .value("disablePlayerResponse", false);

        final byte[] body = JsonWriter.string(builder.done())
                .getBytes(StandardCharsets.UTF_8);

        final String url = YOUTUBEI_V1_GAPIS_URL + "reel/reel_item_watch" + "?"
                + DISABLE_PRETTY_PRINT_PARAMETER + "&t=" + generateTParameter() + "&id=" + videoId
                + "&$fields=playerResponse";

        return JsonUtils.toJsonObject(getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(url, headers, body, localization)))
                .getObject("playerResponse");
    }

    public static JsonObject getIosPlayerResponse(@Nonnull final ContentCountry contentCountry,
                                                  @Nonnull final Localization localization,
                                                  @Nonnull final String videoId,
                                                  @Nonnull final String cpn,
                                                  @Nullable final PoTokenResult iosPoTokenResult)
            throws IOException, ExtractionException {
        final InnertubeClientRequestInfo innertubeClientRequestInfo =
                InnertubeClientRequestInfo.ofIosClient();

        final Map<String, List<String>> headers =
                getMobileClientHeaders(getIosUserAgent(localization));

        // We must always pass a valid visitorData to get valid player responses, which needs to be
        // got from YouTube
        innertubeClientRequestInfo.clientInfo.visitorData = iosPoTokenResult == null
                ? YoutubeParsingHelper.getVisitorDataFromInnertube(innertubeClientRequestInfo,
                        localization, contentCountry, headers, YOUTUBEI_V1_URL, null, false)
                : iosPoTokenResult.visitorData;

        final JsonBuilder<JsonObject> builder = prepareJsonBuilder(localization, contentCountry,
                innertubeClientRequestInfo, null);

        addVideoIdCpnAndOkChecks(builder, videoId, cpn);

        if (iosPoTokenResult != null) {
            addPoToken(builder, iosPoTokenResult.playerRequestPoToken);
        }

        final byte[] body = JsonWriter.string(builder.done())
                .getBytes(StandardCharsets.UTF_8);

        final String url = YOUTUBEI_V1_GAPIS_URL + PLAYER + "?" + DISABLE_PRETTY_PRINT_PARAMETER
                + "&t=" + generateTParameter() + "&id=" + videoId;

        return JsonUtils.toJsonObject(getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(url, headers, body, localization)));
    }

    private static void addVideoIdCpnAndOkChecks(@Nonnull final JsonBuilder<JsonObject> builder,
                                                 @Nonnull final String videoId,
                                                 @Nullable final String cpn) {
        builder.value(VIDEO_ID, videoId);

        if (cpn != null) {
            builder.value(CPN, cpn);
        }

        builder.value(CONTENT_CHECK_OK, true)
                .value(RACY_CHECK_OK, true);
    }

    private static void addPlaybackContext(@Nonnull final JsonBuilder<JsonObject> builder,
                                           @Nonnull final String referer,
                                           final int signatureTimestamp) {
        builder.object("playbackContext")
                .object("contentPlaybackContext")
                .value("signatureTimestamp", signatureTimestamp)
                .value("referer", referer)
                .end()
                .end();
    }

    private static void addPoToken(@Nonnull final JsonBuilder<JsonObject> builder,
                                   @Nonnull final String poToken) {
        builder.object(SERVICE_INTEGRITY_DIMENSIONS)
                .value(PO_TOKEN, poToken)
                .end();
    }

    @Nonnull
    private static Map<String, List<String>> getMobileClientHeaders(
            @Nonnull final String userAgent) {
        return Map.of("User-Agent", List.of(userAgent),
                "X-Goog-Api-Format-Version", List.of("2"));
    }
}
