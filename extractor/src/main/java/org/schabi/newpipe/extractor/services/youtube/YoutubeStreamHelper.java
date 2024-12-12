package org.schabi.newpipe.extractor.services.youtube;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.schabi.newpipe.extractor.NewPipe.getDownloader;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.CONTENT_CHECK_OK;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.CPN;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.RACY_CHECK_OK;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.VIDEO_ID;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.generateTParameter;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonAndroidPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonIosPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getValidJsonResponseBody;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getYouTubeHeaders;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareAndroidMobileJsonBuilder;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareIosMobileJsonBuilder;

public final class YoutubeStreamHelper {

    private static final String STREAMING_DATA = "streamingData";
    private static final String PLAYER = "player";
    private static final String SERVICE_INTEGRITY_DIMENSIONS = "serviceIntegrityDimensions";
    private static final String PO_TOKEN = "poToken";

    private YoutubeStreamHelper() {
    }

    @Nonnull
    public static JsonObject getWebMetadataPlayerResponse(
            @Nonnull final Localization localization,
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final String videoId) throws IOException, ExtractionException {
        final byte[] body = JsonWriter.string(
                        prepareDesktopJsonBuilder(localization, contentCountry)
                                .value(VIDEO_ID, videoId)
                                .value(CONTENT_CHECK_OK, true)
                                .value(RACY_CHECK_OK, true)
                                .done())
                .getBytes(StandardCharsets.UTF_8);
        final String url = YOUTUBEI_V1_URL + "player" + "?" + DISABLE_PRETTY_PRINT_PARAMETER
                + "&$fields=microformat,playabilityStatus,storyboards,videoDetails";

        return JsonUtils.toJsonObject(getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(
                        url, getYouTubeHeaders(), body, localization)));
    }

    @Nonnull
    public static JsonObject getWebFullPlayerResponse(
            @Nonnull final Localization localization,
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final String videoId,
            @Nonnull final PoTokenResult webPoTokenResult) throws IOException, ExtractionException {
        final byte[] body = JsonWriter.string(
                        prepareDesktopJsonBuilder(
                                localization,
                                contentCountry,
                                webPoTokenResult.visitorData
                        )
                                .value(VIDEO_ID, videoId)
                                .value(CONTENT_CHECK_OK, true)
                                .value(RACY_CHECK_OK, true)
                                .object(SERVICE_INTEGRITY_DIMENSIONS)
                                .value(PO_TOKEN, webPoTokenResult.poToken)
                                .end()
                                .done())
                .getBytes(StandardCharsets.UTF_8);
        final String url = YOUTUBEI_V1_URL + "player" + "?" + DISABLE_PRETTY_PRINT_PARAMETER;

        return JsonUtils.toJsonObject(getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(
                        url, getYouTubeHeaders(), body, localization)));
    }

    public static JsonObject getAndroidPlayerResponse(
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final Localization localization,
            @Nonnull final String videoId,
            @Nonnull final String androidCpn,
            @Nonnull final PoTokenResult androidPoTokenResult
    )
            throws IOException, ExtractionException {
        final byte[] mobileBody = JsonWriter.string(
                        prepareAndroidMobileJsonBuilder(
                                localization,
                                contentCountry,
                                androidPoTokenResult.visitorData
                        )
                                .value(VIDEO_ID, videoId)
                                .value(CPN, androidCpn)
                                .value(CONTENT_CHECK_OK, true)
                                .value(RACY_CHECK_OK, true)
                                .object(SERVICE_INTEGRITY_DIMENSIONS)
                                .value(PO_TOKEN, androidPoTokenResult.poToken)
                                .end()
                                .done())
                .getBytes(StandardCharsets.UTF_8);

        return getJsonAndroidPostResponse(
                "player",
                mobileBody,
                localization,
                "&t=" + generateTParameter() + "&id=" + videoId);
    }

    public static JsonObject getAndroidReelPlayerResponse(
            @Nonnull final ContentCountry contentCountry,
            @Nonnull final Localization localization,
            @Nonnull final String videoId,
            @Nonnull final String androidCpn
    )
            throws IOException, ExtractionException {
        final byte[] mobileBody = JsonWriter.string(
                        prepareAndroidMobileJsonBuilder(localization, contentCountry, null)
                                .object("playerRequest")
                                .value(VIDEO_ID, videoId)
                                .end()
                                .value("disablePlayerResponse", false)
                                .value(VIDEO_ID, videoId)
                                .value(CPN, androidCpn)
                                .value(CONTENT_CHECK_OK, true)
                                .value(RACY_CHECK_OK, true)
                                .done())
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
                                                  @Nonnull final String iosCpn)
            throws IOException, ExtractionException {
        final byte[] mobileBody = JsonWriter.string(
                        prepareIosMobileJsonBuilder(localization, contentCountry)
                                .value(VIDEO_ID, videoId)
                                .value(CPN, iosCpn)
                                .value(CONTENT_CHECK_OK, true)
                                .value(RACY_CHECK_OK, true)
                                .done())
                .getBytes(StandardCharsets.UTF_8);

        return getJsonIosPostResponse(PLAYER,
                mobileBody, localization, "&t=" + generateTParameter()
                        + "&id=" + videoId);
    }
}
