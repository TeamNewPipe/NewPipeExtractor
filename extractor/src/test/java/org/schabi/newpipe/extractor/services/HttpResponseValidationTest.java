package org.schabi.newpipe.extractor.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.HttpResponseException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampCommentsExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampPlaylistExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampRadioExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampRadioStreamExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampSearchExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampStreamExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCConferenceExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCConferenceKiosk;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCRecentKiosk;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCSearchExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCStreamExtractor;
import org.schabi.newpipe.extractor.services.peertube.PeertubeInstance;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeAccountExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelTabExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeCommentsExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubePlaylistExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeSearchExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeStreamExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeTrendingExtractor;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudCommentsExtractor;
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudSearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.DeliveryType;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;
import org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeFeedExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

class HttpResponseValidationTest {
    private static final String URL = "https://example.com/resource";
    private static final Page PAGE = new Page(URL);
    private Downloader previousDownloader;
    private Localization previousLocalization;
    private ContentCountry previousCountry;

    @BeforeEach
    void saveDownloader() {
        previousDownloader = NewPipe.getDownloader();
        previousLocalization = NewPipe.getPreferredLocalization();
        previousCountry = NewPipe.getPreferredContentCountry();
    }

    @AfterEach
    void restoreDownloader() {
        NewPipe.init(previousDownloader, previousLocalization, previousCountry);
    }

    static Stream<Arguments> failingRequests() {
        return operations().flatMap(operation -> Stream.of(403, 429, 503)
                .map(code -> Arguments.of(operation[0], operation[1], code)));
    }

    private static Stream<Object[]> operations() {
        return Stream.of(
                operation("Bandcamp track",
                        () -> new BandcampStreamExtractor(Bandcamp, link()).fetchPage()),
                operation("Bandcamp album",
                        () -> new BandcampPlaylistExtractor(Bandcamp, list()).fetchPage()),
                operation("Bandcamp comments",
                        () -> new BandcampCommentsExtractor(Bandcamp, list()).fetchPage()),
                operation("Bandcamp search",
                        () -> new BandcampSearchExtractor(Bandcamp, query()).getPage(PAGE)),
                operation("Bandcamp featured",
                        () -> new BandcampFeaturedExtractor(
                                Bandcamp, list(), "Featured").fetchPage()),
                operation("Bandcamp featured continuation",
                        () -> new BandcampFeaturedExtractor(
                                Bandcamp, list(), "Featured").getPage(PAGE)),
                operation("Bandcamp radio list",
                        () -> new BandcampRadioExtractor(Bandcamp, list(), "Radio").fetchPage()),
                operation("Bandcamp radio stream",
                        () -> new BandcampRadioStreamExtractor(
                                Bandcamp, link()).fetchPage()),
                operation("Bandcamp suggestions",
                        () -> Bandcamp.getSuggestionExtractor().suggestionList("test")),
                operation("Bandcamp artist details",
                        () -> BandcampExtractorHelper.getArtistDetails("1")),
                operation("Bandcamp stream URL",
                        () -> BandcampExtractorHelper.getStreamUrlFromIds(1, 2, "track")),
                operation("Bandcamp channel URL",
                        () -> BandcampChannelLinkHandlerFactory.getInstance()
                                .getId(URL)),
                operation("Bandcamp custom domain",
                        () -> BandcampExtractorHelper.isArtistDomain(URL)),
                operation("MediaCCC conference",
                        () -> new MediaCCCConferenceExtractor(MediaCCC, list()).fetchPage()),
                operation("MediaCCC conference list",
                        () -> new MediaCCCConferenceKiosk(
                                MediaCCC, list(), "conferences").fetchPage()),
                operation("MediaCCC recent",
                        () -> new MediaCCCRecentKiosk(MediaCCC, list(), "recent").fetchPage()),
                operation("MediaCCC search",
                        () -> new MediaCCCSearchExtractor(MediaCCC, query()).fetchPage()),
                operation("MediaCCC stream",
                        () -> new MediaCCCStreamExtractor(MediaCCC, link()).fetchPage()),
                operation("PeerTube instance",
                        () -> new PeertubeInstance("https://example.com").fetchInstanceMetaData()),
                operation("PeerTube account",
                        () -> new PeertubeAccountExtractor(PeerTube, list()).fetchPage()),
                operation("PeerTube channel",
                        () -> new PeertubeChannelExtractor(PeerTube, list()).fetchPage()),
                operation("PeerTube channel tab",
                        () -> new PeertubeChannelTabExtractor(
                                PeerTube, list()).getPage(PAGE)),
                operation("PeerTube comments",
                        () -> new PeertubeCommentsExtractor(PeerTube, list()).getPage(PAGE)),
                operation("PeerTube playlist",
                        () -> new PeertubePlaylistExtractor(PeerTube, list()).fetchPage()),
                operation("PeerTube playlist continuation",
                        () -> new PeertubePlaylistExtractor(PeerTube, list()).getPage(PAGE)),
                operation("PeerTube search",
                        () -> new PeertubeSearchExtractor(PeerTube, query()).getPage(PAGE)),
                operation("PeerTube trending",
                        () -> new PeertubeTrendingExtractor(PeerTube, list(), "Trending").getPage(PAGE)),
                operation("PeerTube stream",
                        () -> new PeertubeStreamExtractor(PeerTube, link()).fetchPage()),
                operation("SoundCloud comments",
                        () -> new SoundcloudCommentsExtractor(SoundCloud, list()).getPage(PAGE)),
                operation("SoundCloud search",
                        () -> new SoundcloudSearchExtractor(SoundCloud, query()).fetchPage()),
                operation("SoundCloud search continuation",
                        () -> new SoundcloudSearchExtractor(SoundCloud, query()).getPage(PAGE)),
                operation("SoundCloud stream list",
                        () -> SoundcloudParsingHelper.getStreamsFromApi(
                                new StreamInfoItemsCollector(SoundCloud.getServiceId()), URL)),
                operation("SoundCloud item list",
                        () -> SoundcloudParsingHelper.getInfoItemsFromApi(
                                new MultiInfoItemsCollector(SoundCloud.getServiceId()), URL)),
                operation("YouTube feed",
                        () -> new YoutubeFeedExtractor(YouTube, list()).fetchPage()),
                operation("YouTube suggestions",
                        () -> YouTube.getSuggestionExtractor().suggestionList("test")),
                operation("YouTube stream initialization GET",
                        () -> YoutubeDashManifestCreatorsUtils.getInitializationResponse(
                                URL + "?c=OTHER", ItagItem.getItag(140),
                                DeliveryType.OTF)),
                operation("YouTube stream initialization WEB",
                        () -> YoutubeDashManifestCreatorsUtils.getInitializationResponse(
                                URL + "?id=test&c=WEB", ItagItem.getItag(140),
                                DeliveryType.OTF)),
                operation("YouTube stream initialization POST",
                        () -> YoutubeDashManifestCreatorsUtils.getInitializationResponse(
                                URL + "?id=test&c=VISIONOS", ItagItem.getItag(140),
                                DeliveryType.OTF)));
    }

    @ParameterizedTest(name = "{0}: HTTP {2}")
    @MethodSource("failingRequests")
    void reportsHttpErrorBeforeParsing(final String name, final Executable operation, final int code) {
        final List<String> urls = new ArrayList<>();
        NewPipe.init(new Downloader() {
            @Override
            public Response execute(final Request request) {
                urls.add(request.url());
                // Cover a JSON error object, an empty error and a proxy's HTML error page.
                final String body = code == 403 ? "{\"error\":\"forbidden\"}"
                        : code == 429 ? "" : "<html><body>Service unavailable</body></html>";
                return new Response(code, "test error", Map.of(), body, request.url());
            }
        });

        final Exception failure = assertThrows(Exception.class, operation);
        Throwable cause = failure;
        while (cause != null && !(cause instanceof HttpResponseException)) {
            cause = cause.getCause();
        }
        assertNotNull(cause, () -> "HTTP status was lost: " + failure);
        assertFalse(urls.isEmpty());
        assertTrue(cause.getMessage().contains(code + " - test error"));
        assertTrue(cause.getMessage().contains(urls.get(urls.size() - 1)));
    }

    private static Object[] operation(final String name, final Executable executable) {
        return new Object[] {name, executable};
    }

    private static LinkHandler link() {
        return new LinkHandler(URL, URL, "1");
    }

    private static ListLinkHandler list() {
        return new ListLinkHandler(URL, URL, "1", List.of(), "");
    }

    private static SearchQueryHandler query() {
        return new SearchQueryHandler(URL, URL, "test", List.of(), "");
    }
}
