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
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.ANDROID_CLIENT_NAME;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.ANDROID_CLIENT_VERSION;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.DESKTOP_CLIENT_PLATFORM;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.EMBED_CLIENT_SCREEN;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.IOS_CLIENT_NAME;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.IOS_CLIENT_VERSION;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.IOS_DEVICE_MODEL;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.IOS_OS_VERSION;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.MOBILE_CLIENT_PLATFORM;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.TVHTML5_CLIENT_ID;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.TVHTML5_CLIENT_NAME;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.TVHTML5_CLIENT_PLATFORM;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.TVHTML5_CLIENT_VERSION;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.TVHTML5_DEVICE_MAKE;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.TVHTML5_DEVICE_MODEL_AND_OS_NAME;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.TVHTML5_USER_AGENT;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.WATCH_CLIENT_SCREEN;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.WEB_CLIENT_NAME;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.WEB_EMBEDDED_CLIENT_ID;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.WEB_EMBEDDED_CLIENT_NAME;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.WEB_EMBEDDED_CLIENT_VERSION;
import static org.schabi.newpipe.extractor.services.youtube.ClientsConstants.WEB_REMIX_HARDCODED_CLIENT_VERSION;
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
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

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
        final JsonBuilder<JsonObject> builder = prepareJsonBuilder(
                localization,
                contentCountry,
                WEB_CLIENT_NAME,
                getClientVersion(),
                WATCH_CLIENT_SCREEN,
                DESKTOP_CLIENT_PLATFORM,
                YoutubeParsingHelper.randomVisitorData(contentCountry),
                null,
                null,
                null,
                null,
                null,
                -1);

        addVideoIdCpnAndOkChecks(builder, videoId, null);

        final byte[] body = JsonWriter.string(builder.done())
                .getBytes(StandardCharsets.UTF_8);

        final String url = YOUTUBEI_V1_URL + PLAYER + "?" + DISABLE_PRETTY_PRINT_PARAMETER
                + "&$fields=microformat,playabilityStatus,storyboards,videoDetails";

        return JsonUtils.toJsonObject(getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(
                        url, getYouTubeHeaders(), body, localization)));
    }

    @Nonnull
    public static JsonObject getTvHtml5PlayerResponse(
            @Nonnull final Localization localization,
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final String videoId,
            @Nonnull final String cpn) throws IOException, ExtractionException {
        final JsonBuilder<JsonObject> builder = prepareJsonBuilder(
                localization,
                contentCountry,
                TVHTML5_CLIENT_NAME,
                TVHTML5_CLIENT_VERSION,
                WATCH_CLIENT_SCREEN,
                TVHTML5_CLIENT_PLATFORM,
                YoutubeParsingHelper.randomVisitorData(contentCountry),
                TVHTML5_DEVICE_MAKE,
                TVHTML5_DEVICE_MODEL_AND_OS_NAME,
                TVHTML5_DEVICE_MODEL_AND_OS_NAME,
                "",
                null,
                -1);
        addVideoIdCpnAndOkChecks(builder, videoId, cpn);

        final byte[] body = JsonWriter.string(builder.done())
                .getBytes(StandardCharsets.UTF_8);

        final String url = YOUTUBEI_V1_URL + PLAYER + "?" + DISABLE_PRETTY_PRINT_PARAMETER;

        final Map<String, List<String>> headers = new HashMap<>(
                getClientHeaders(TVHTML5_CLIENT_ID, TVHTML5_CLIENT_VERSION));
        headers.putAll(getOriginReferrerHeaders("https://www.youtube.com"));
        headers.put("User-Agent", List.of(TVHTML5_USER_AGENT));

        return JsonUtils.toJsonObject(getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(
                        url, headers, body, localization)));
    }

    @Nonnull
    public static JsonObject getWebFullPlayerResponse(
            @Nonnull final Localization localization,
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final String videoId,
            @Nonnull final String cpn,
            @Nonnull final PoTokenResult webPoTokenResult,
            final int signatureTimestamp) throws IOException, ExtractionException {
        final JsonBuilder<JsonObject> builder = prepareJsonBuilder(
                localization,
                contentCountry,
                WEB_CLIENT_NAME,
                getClientVersion(),
                WATCH_CLIENT_SCREEN,
                DESKTOP_CLIENT_PLATFORM,
                webPoTokenResult.visitorData,
                null,
                null,
                null,
                null,
                null,
                -1);

        addVideoIdCpnAndOkChecks(builder, videoId, cpn);

        addPlaybackContext(
                builder,
                BASE_YT_DESKTOP_WATCH_URL + videoId,
                signatureTimestamp);

        addPoToken(builder, webPoTokenResult.playerRequestPoToken);

        final byte[] body = JsonWriter.string(builder.end().done())
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
        final JsonBuilder<JsonObject> builder = prepareJsonBuilder(
                localization,
                contentCountry,
                WEB_EMBEDDED_CLIENT_NAME,
                WEB_REMIX_HARDCODED_CLIENT_VERSION,
                EMBED_CLIENT_SCREEN,
                DESKTOP_CLIENT_PLATFORM,
                webEmbeddedPoTokenResult == null
                        ? YoutubeParsingHelper.randomVisitorData(contentCountry)
                        : webEmbeddedPoTokenResult.visitorData,
                null,
                null,
                null,
                null,
                BASE_YT_DESKTOP_WATCH_URL + videoId,
                -1);

        addVideoIdCpnAndOkChecks(builder, videoId, cpn);

        addPlaybackContext(
                builder,
                BASE_YT_DESKTOP_WATCH_URL + videoId,
                signatureTimestamp);

        if (webEmbeddedPoTokenResult != null) {
            addPoToken(builder, webEmbeddedPoTokenResult.playerRequestPoToken);
        }

        final byte[] body = JsonWriter.string(builder.done())
                .getBytes(StandardCharsets.UTF_8);
        final String url = YOUTUBEI_V1_URL + PLAYER + "?" + DISABLE_PRETTY_PRINT_PARAMETER;

        final Map<String, List<String>> headers = new HashMap<>(
                getClientHeaders(WEB_EMBEDDED_CLIENT_ID, WEB_EMBEDDED_CLIENT_VERSION));
        headers.putAll(getOriginReferrerHeaders("https://www.youtube.com"));

        return JsonUtils.toJsonObject(getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(
                        url, headers, body, localization)));
    }

    public static JsonObject getAndroidPlayerResponse(
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final Localization localization,
            @Nonnull final String videoId,
            @Nonnull final String cpn,
            @Nonnull final PoTokenResult androidPoTokenResult)
            throws IOException, ExtractionException {

        final JsonBuilder<JsonObject> builder = prepareJsonBuilder(
                localization,
                contentCountry,
                ANDROID_CLIENT_NAME,
                ANDROID_CLIENT_VERSION,
                WATCH_CLIENT_SCREEN,
                MOBILE_CLIENT_PLATFORM,
                androidPoTokenResult.visitorData,
                null,
                null,
                "Android",
                "15",
                null,
                35);

        addVideoIdCpnAndOkChecks(builder, videoId, cpn);

        addPoToken(builder, androidPoTokenResult.playerRequestPoToken);

        final byte[] body = JsonWriter.string(builder.end().done())
                .getBytes(StandardCharsets.UTF_8);

        return getJsonAndroidPostResponse(
                PLAYER,
                body,
                localization,
                "&t=" + generateTParameter() + "&id=" + videoId);
    }

    public static JsonObject getAndroidReelPlayerResponse(
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final Localization localization,
            @Nonnull final String videoId,
            @Nonnull final String cpn) throws IOException, ExtractionException {
        final JsonBuilder<JsonObject> builder = prepareJsonBuilder(
                localization,
                contentCountry,
                ANDROID_CLIENT_NAME,
                ANDROID_CLIENT_VERSION,
                WATCH_CLIENT_SCREEN,
                MOBILE_CLIENT_PLATFORM,
                YoutubeParsingHelper.randomVisitorData(contentCountry),
                null,
                null,
                "Android",
                "15",
                null,
                35);

        addVideoIdCpnAndOkChecks(builder, videoId, cpn);

        builder.object("playerRequest")
                .value(VIDEO_ID, videoId)
                .end()
                .value("disablePlayerResponse", false);

        final byte[] mobileBody = JsonWriter.string(builder.done())
                .getBytes(StandardCharsets.UTF_8);

        final JsonObject androidPlayerResponse = getJsonAndroidPostResponse(
                "reel/reel_item_watch",
                mobileBody,
                localization,
                "&t=" + generateTParameter() + "&id=" + videoId + "&$fields=playerResponse");

        return androidPlayerResponse.getObject("playerResponse");
    }

    public static JsonObject getIosPlayerResponse(@Nonnull final ContentCountry contentCountry,
                                                  @Nonnull final Localization localization,
                                                  @Nonnull final String videoId,
                                                  @Nonnull final String cpn,
                                                  @Nullable final PoTokenResult iosPoTokenResult)
            throws IOException, ExtractionException {
        final boolean noPoTokenResult = iosPoTokenResult == null;
        final JsonBuilder<JsonObject> builder = prepareJsonBuilder(
                localization,
                contentCountry,
                IOS_CLIENT_NAME,
                IOS_CLIENT_VERSION,
                WATCH_CLIENT_SCREEN,
                MOBILE_CLIENT_PLATFORM,
                noPoTokenResult
                        ? YoutubeParsingHelper.randomVisitorData(contentCountry)
                        : iosPoTokenResult.visitorData,
                "Apple",
                IOS_DEVICE_MODEL,
                "iOS",
                IOS_OS_VERSION,
                null,
                -1);

        addVideoIdCpnAndOkChecks(builder, videoId, cpn);
        if (!noPoTokenResult) {
            addPoToken(builder, iosPoTokenResult.playerRequestPoToken);
        }

        final byte[] mobileBody = JsonWriter.string(builder.done())
                .getBytes(StandardCharsets.UTF_8);

        return getJsonIosPostResponse(
                mobileBody, localization, "&t=" + generateTParameter()
                        + "&id=" + videoId + "&fields=streamingData.hlsManifestUrl");
    }

    public static JsonObject getJsonAndroidPostResponse(final String endpoint,
                                                        final byte[] body,
                                                        @Nonnull final Localization localization,
                                                        @Nullable final String endPartOfUrlRequest)
            throws IOException, ExtractionException {
        return getMobilePostResponse(endpoint, body, localization,
                getAndroidUserAgent(localization), endPartOfUrlRequest);
    }

    private static JsonObject getJsonIosPostResponse(final byte[] body,
                                                     @Nonnull final Localization localization,
                                                     @Nullable final String endPartOfUrlRequest)
            throws IOException, ExtractionException {
        return getMobilePostResponse(YoutubeStreamHelper.PLAYER, body, localization,
                getIosUserAgent(localization),
                endPartOfUrlRequest);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    @Nonnull
    private static JsonBuilder<JsonObject> prepareJsonBuilder(
            @Nonnull final Localization localization,
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final String clientName,
            @Nonnull final String clientVersion,
            @Nonnull final String clientScreen,
            @Nonnull final String platform,
            @Nonnull final String visitorData,
            @Nullable final String deviceMake,
            @Nullable final String deviceModel,
            @Nullable final String osName,
            @Nullable final String osVersion,
            @Nullable final String embedUrl,
            final int androidSdkVersion) {
        final JsonBuilder<JsonObject> builder = JsonObject.builder()
                .object("context")
                .object("client")
                .value("clientName", clientName)
                .value("clientVersion", clientVersion)
                .value("clientScreen", clientScreen)
                .value("platform", platform)
                .value("visitorData", visitorData);

        if (deviceMake != null) {
            builder.value("deviceMake", deviceMake);
        }
        if (deviceModel != null) {
            builder.value("deviceModel", deviceModel);
        }
        if (osName != null) {
            builder.value("osName", osName);
        }
        if (osVersion != null) {
            builder.value("osVersion", osVersion);
        }
        if (androidSdkVersion > 0) {
            builder.value("androidSdkVersion", androidSdkVersion);
        }

        builder.value("hl", localization.getLocalizationCode())
                .value("gl", contentCountry.getCountryCode())
                .value("utcOffsetMinutes", 0)
                .end();

        if (embedUrl != null) {
            builder.object("thirdParty")
                    .value("embedUrl", embedUrl)
                    .end();
        }

        builder.object("request")
                .array("internalExperimentFlags")
                .end()
                .value("useSsl", true)
                .end()
                .object("user")
                // TODO: provide a way to enable restricted mode with:
                //  .value("enableSafetyMode", boolean)
                .value("lockedSafetyMode", false)
                .end()
                .end();

        return builder;
    }

    private static JsonObject getMobilePostResponse(@Nonnull final String endpoint,
                                                    final byte[] body,
                                                    @Nonnull final Localization localization,
                                                    @Nonnull final String userAgent,
                                                    @Nullable final String endPartOfUrlRequest)
            throws IOException, ExtractionException {
        final Map<String, List<String>> headers = Map.of("User-Agent", List.of(userAgent),
                "X-Goog-Api-Format-Version", List.of("2"));

        final String baseEndpointUrl = YOUTUBEI_V1_GAPIS_URL + endpoint + "?"
                + DISABLE_PRETTY_PRINT_PARAMETER;

        return JsonUtils.toJsonObject(getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(isNullOrEmpty(endPartOfUrlRequest)
                                ? baseEndpointUrl
                                : baseEndpointUrl + endPartOfUrlRequest,
                        headers, body, localization)));
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
}
