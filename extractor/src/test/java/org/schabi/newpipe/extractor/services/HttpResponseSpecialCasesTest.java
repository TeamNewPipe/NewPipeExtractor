package org.schabi.newpipe.extractor.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.HttpResponseException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampChannelExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeStreamExtractor;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.DeliveryType;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;
import org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeFeedExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

class HttpResponseSpecialCasesTest {
    private static final String BASE = "https://example.com";
    private static final LinkHandler LINK = new LinkHandler(
            BASE + "/videos/watch/test", BASE + "/videos/watch/test", "test");
    private Downloader previousDownloader;
    private Localization previousLocalization;
    private ContentCountry previousCountry;
    private Field clientIdField;
    private Object previousClientId;

    @BeforeEach
    void saveState() throws Exception {
        previousDownloader = NewPipe.getDownloader();
        previousLocalization = NewPipe.getPreferredLocalization();
        previousCountry = NewPipe.getPreferredContentCountry();
        // SoundCloud has no public cache reset; isolate the client ID used by the redirect test.
        clientIdField = SoundcloudParsingHelper.class.getDeclaredField("clientId");
        clientIdField.setAccessible(true);
        previousClientId = clientIdField.get(null);
    }

    @AfterEach
    void restoreState() throws Exception {
        clientIdField.set(null, previousClientId);
        NewPipe.init(previousDownloader, previousLocalization, previousCountry);
    }

    @Test
    void missingCustomDomainIsNotAnArtist() throws Exception {
        respond(request -> response(request, 404, "Not Found", ""));
        assertFalse(BandcampExtractorHelper.isArtistDomain(BASE));
    }

    @Test
    void bandcampBannerDownloadKeepsHttpCause() throws Exception {
        respond(request -> request.httpMethod().equals("POST")
                ? response(request, 200, "OK", "{\"bandcamp_url\":\"https://artist.bandcamp.com\"}")
                : response(request, 503, "Service Unavailable", ""));
        final BandcampChannelExtractor extractor = new BandcampChannelExtractor(Bandcamp,
                new ListLinkHandler(BASE, BASE, "1", List.of(), ""));
        extractor.fetchPage();
        assertHttpCause(assertThrows(ParsingException.class, extractor::getBanners), 503);
    }

    @Test
    void soundcloudHead403StillResolvesRedirect() throws Exception {
        clientIdField.set(null, null);
        final List<String> urls = new ArrayList<>();
        respond(request -> {
            urls.add(request.url());
            if (request.httpMethod().equals("HEAD")) {
                return new Response(403, "Forbidden", Map.of(), "",
                        "https://soundcloud.com/artist/track?tracking=1");
            }
            if (request.url().equals("https://soundcloud.com")) {
                return response(request, 200, "OK",
                        "<script src=\"https://a.sndcdn.com/assets/test.js\"></script>");
            }
            if (request.url().endsWith("test.js")) {
                return response(request, 206, "Partial Content", ",client_id:\"test-client\"");
            }
            if (request.url().startsWith("https://api-widget.soundcloud.com/resolve?")) {
                return response(request, 200, "OK", "{\"id\":123}");
            }
            throw new AssertionError("Unexpected request " + request.url());
        });
        assertEquals("123", SoundcloudParsingHelper.resolveIdWithWidgetApi(
                "https://on.soundcloud.com/abc123"));
        assertTrue(urls.stream().anyMatch(url -> url.contains("artist%2Ftrack")));
        assertTrue(urls.stream().noneMatch(url -> url.contains("tracking")));
    }

    @ParameterizedTest
    @ValueSource(ints = {404, 429, 503})
    void soundcloudHeadErrorsAreNotTreatedAsRedirects(final int status) {
        respond(request -> response(request, status, "failure", ""));
        assertHttpCause(assertThrows(ParsingException.class,
                () -> SoundcloudParsingHelper.resolveIdWithWidgetApi(
                        "https://on.soundcloud.com/abc123")), status);
    }

    @Test
    void soundcloudAcceptsSuccessfulPartialResponses() throws Exception {
        respond(request -> response(request, 206, "Partial Content",
                "{\"collection\":[],\"next_href\":null}"));
        final StreamInfoItemsCollector collector =
                new StreamInfoItemsCollector(SoundCloud.getServiceId());
        assertEquals("", SoundcloudParsingHelper.getStreamsFromApi(collector, BASE));
        assertTrue(collector.getItems().isEmpty());
    }

    @Test
    void unavailablePeertubeStreamKeepsContentException() throws Exception {
        respond(request -> response(request, 404, "Not Found", "not JSON"));
        final PeertubeStreamExtractor extractor = new PeertubeStreamExtractor(PeerTube, LINK);
        assertHttpCause(assertThrows(ContentNotAvailableException.class, extractor::fetchPage), 404);
    }

    @Test
    void unsupportedPeertubeSubApisRemainOptional() throws Exception {
        respond(request -> response(request, 400, "Bad Request", ""));
        final PeertubeStreamExtractor extractor = new PeertubeStreamExtractor(PeerTube, LINK);
        assertTrue(extractor.getStreamSegments().isEmpty());
        assertTrue(extractor.getFrames().isEmpty());
    }

    @Test
    void peertubeSubApiFailureKeepsHttpCause() throws Exception {
        respond(request -> response(request, 503, "Service Unavailable", ""));
        final PeertubeStreamExtractor extractor = new PeertubeStreamExtractor(PeerTube, LINK);
        assertHttpCause(assertThrows(ParsingException.class, extractor::getStreamSegments), 503);
        assertHttpCause(assertThrows(ParsingException.class, extractor::getFrames), 503);
    }

    @Test
    void peertubeKeepsShortDescriptionWhenFullDescriptionFails() throws Exception {
        final String description = "x".repeat(247) + "...";
        respond(request -> {
            if (request.url().endsWith("/description")) {
                return response(request, 503, "Service Unavailable",
                        "{\"description\":\"proxy error\"}");
            }
            if (request.url().endsWith("/captions")) {
                return response(request, 200, "OK", "{\"data\":[]}");
            }
            return response(request, 200, "OK", "{\"description\":\"" + description + "\"}");
        });
        final PeertubeStreamExtractor extractor = new PeertubeStreamExtractor(PeerTube, LINK);
        extractor.fetchPage();
        assertEquals(description, extractor.getDescription().content());
    }

    @Test
    void peertubeCaptionFailureIsReportedWhenSubtitlesAreRequested() throws Exception {
        respond(request -> request.url().endsWith("/captions")
                ? response(request, 503, "Service Unavailable", "{\"data\":[]}")
                : response(request, 200, "OK", "{}"));
        final PeertubeStreamExtractor extractor = new PeertubeStreamExtractor(PeerTube, LINK);
        extractor.fetchPage();
        assertHttpCause(assertThrows(ParsingException.class, extractor::getSubtitlesDefault), 503);
    }

    @Test
    void youtubeFeed404StillMeansUnavailable() {
        respond(request -> response(request, 404, "Not Found", ""));
        assertThrows(ContentNotAvailableException.class, () -> new YoutubeFeedExtractor(YouTube,
                new ListLinkHandler(BASE, BASE, "channel/UCtest", List.of(), "")).fetchPage());
    }

    @Test
    void youtubeWebStreamingRedirectStillFollowsSuccessfulTextResponse() throws Exception {
        final List<String> urls = new ArrayList<>();
        respond(request -> {
            urls.add(request.url());
            return urls.size() == 1
                    ? new Response(200, "OK", Map.of("Content-Type", List.of("text/plain")),
                            BASE + "/redirected?id=test&c=WEB", request.url())
                    : new Response(200, "OK", Map.of("Content-Type", List.of("audio/mp4")),
                            "stream", request.url());
        });
        final Response result = YoutubeDashManifestCreatorsUtils.getInitializationResponse(
                BASE + "/stream?id=test&c=WEB", ItagItem.getItag(140), DeliveryType.OTF);
        assertEquals(BASE + "/redirected?id=test&c=WEB", result.latestUrl());
        assertEquals(2, urls.size());
    }

    private static void respond(final Function<Request, Response> function) {
        NewPipe.init(new Downloader() {
            @Override
            public Response execute(final Request request) {
                return function.apply(request);
            }
        });
    }

    private static Response response(final Request request, final int status, final String message,
                                     final String body) {
        return new Response(status, message, Map.of(), body, request.url());
    }

    private static void assertHttpCause(final Throwable exception, final int code) {
        Throwable cause = exception;
        while (cause != null && !(cause instanceof HttpResponseException)) {
            cause = cause.getCause();
        }
        assertNotNull(cause, () -> "HTTP status was lost: " + exception);
        assertTrue(cause.getMessage().contains(code + " - "));
    }
}
