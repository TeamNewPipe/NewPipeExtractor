package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.HttpResponseException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

class YoutubeHttpResponseTest {
    private static final String PLAYER_PATH = "/s/player/abcdef12/player_ias.vflset/en_GB/base.js";
    private static final String IFRAME_BODY = "player\\/abcdef12\\/";
    private static final String EMBED_BODY = "<script src=\"" + PLAYER_PATH + "\"></script>";
    private static final String VERSION_BODY = "\"INNERTUBE_CONTEXT_CLIENT_VERSION\":\"2.20261117.00\"";
    private final Map<Field, Object> savedCache = new LinkedHashMap<>();
    private Downloader previousDownloader;
    private Localization previousLocalization;
    private ContentCountry previousCountry;

    @BeforeEach
    void saveState() throws Exception {
        previousDownloader = NewPipe.getDownloader();
        previousLocalization = NewPipe.getPreferredLocalization();
        previousCountry = NewPipe.getPreferredContentCountry();
        // Isolate all client-version caches, including those without public reset methods.
        for (final String name : List.of("clientVersion", "clientVersionExtracted",
                "youtubeMusicClientVersion", "hardcodedClientVersionValid", "numberGenerator")) {
            final Field field = YoutubeParsingHelper.class.getDeclaredField(name);
            field.setAccessible(true);
            savedCache.put(field, field.get(null));
            field.set(null, name.equals("clientVersionExtracted") ? false
                    : name.equals("hardcodedClientVersionValid") ? Optional.empty()
                    : name.equals("numberGenerator") ? new Random(0) : null);
        }
    }

    @AfterEach
    void restoreState() throws Exception {
        for (final Map.Entry<Field, Object> entry : savedCache.entrySet()) {
            entry.getKey().set(null, entry.getValue());
        }
        NewPipe.init(previousDownloader, previousLocalization, previousCountry);
    }

    @Test
    void rejectsIframeErrorEvenWhenItContainsAPlayerHash() {
        respond(request -> response(request, 503, IFRAME_BODY));
        assertHttpCause(assertThrows(ParsingException.class,
                YoutubeJavaScriptExtractor::extractJavaScriptUrlWithIframeResource));
    }

    @Test
    void rejectsEmbedErrorEvenWhenItContainsAPlayerUrl() {
        respond(request -> response(request, 503, EMBED_BODY));
        assertHttpCause(assertThrows(ParsingException.class,
                () -> YoutubeJavaScriptExtractor.extractJavaScriptUrlWithEmbedWatchPage("test")));
    }

    @Test
    void rejectsPlayerCodeErrorAfterBothDownloadAttempts() {
        final List<String> urls = new ArrayList<>();
        respond(request -> {
            urls.add(request.url());
            if (request.url().endsWith("/iframe_api")) {
                return response(request, 200, IFRAME_BODY);
            }
            if (request.url().contains("/embed/")) {
                return response(request, 200, EMBED_BODY);
            }
            return response(request, 503, "var player = 'error';");
        });
        assertHttpCause(assertThrows(ParsingException.class,
                () -> YoutubeJavaScriptExtractor.extractJavaScriptPlayerCode("test")));
        assertEquals(2, urls.stream().filter(url -> url.endsWith("base.js")).count());
    }

    @Test
    void iframeErrorStillAllowsEmbedFallback() throws Exception {
        respond(request -> request.url().endsWith("/iframe_api")
                ? response(request, 503, "")
                : response(request, 200, request.url().contains("/embed/")
                        ? EMBED_BODY : "player code"));
        assertEquals("player code", YoutubeJavaScriptExtractor.extractJavaScriptPlayerCode("test"));
    }

    @Test
    void rejectsWebClientVersionWhenBothSourcesFail() {
        respond(request -> response(request, 503, "var ytInitialData = {};" + VERSION_BODY));
        assertHttpCause(assertThrows(HttpResponseException.class,
                YoutubeParsingHelper::getClientVersion));
    }

    @Test
    void webClientVersionStillFallsBackToSuccessfulHtml() throws Exception {
        respond(request -> request.url().endsWith("sw.js_data")
                ? response(request, 503, "")
                : response(request, 200, "var ytInitialData = {};" + VERSION_BODY));
        assertEquals("2.20261117.00", YoutubeParsingHelper.getClientVersion());
    }

    @Test
    void rejectsMusicClientVersionWhenBothSourcesFail() {
        respond(request -> response(request, 503, VERSION_BODY));
        assertHttpCause(assertThrows(HttpResponseException.class,
                YoutubeParsingHelper::getYoutubeMusicClientVersion));
    }

    @Test
    void musicClientVersionStillFallsBackToSuccessfulHtml() throws Exception {
        respond(request -> request.url().contains("?ucbcb=1")
                ? response(request, 200, VERSION_BODY) : response(request, 503, VERSION_BODY));
        assertEquals("2.20261117.00", YoutubeParsingHelper.getYoutubeMusicClientVersion());
    }

    @Test
    void clientVersionProbesTreatHttpErrorsAsInvalidVersions() throws Exception {
        respond(request -> response(request, 503, "x".repeat(6000)));
        assertFalse(YoutubeParsingHelper.isHardcodedClientVersionValid());
        assertFalse(YoutubeParsingHelper.isHardcodedYoutubeMusicClientVersionValid());
    }

    private static void respond(final Function<Request, Response> function) {
        NewPipe.init(new Downloader() {
            @Override
            public Response execute(final Request request) {
                return function.apply(request);
            }
        });
    }

    private static Response response(final Request request, final int code, final String body) {
        return new Response(code, code == 200 ? "OK" : "Service Unavailable", Map.of(),
                body, request.url());
    }

    private static void assertHttpCause(final Throwable exception) {
        Throwable cause = exception;
        while (cause != null && !(cause instanceof HttpResponseException)) {
            cause = cause.getCause();
        }
        assertNotNull(cause, () -> "HTTP status was lost: " + exception);
        assertTrue(cause.getMessage().contains("503 - Service Unavailable"));
    }
}
