package org.schabi.newpipe.extractor.services.youtube.search;

import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static java.util.Collections.singletonList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.YoutubeTestsUtils;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;

// Doesn't work with mocks. Makes request with different `dataToSend` I think
public class YoutubeMusicSearchExtractorTest {
    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/musicSearch/";
    private static final String BASE_SEARCH_URL = "music.youtube.com/search?q=";

    public static class MusicSongs extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "mocromaniac";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "musicSongs"));

            extractor = YouTube.getSearchExtractor(QUERY, singletonList(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return BASE_SEARCH_URL + QUERY; }
        @Override public String expectedOriginalUrlContains() { return BASE_SEARCH_URL + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
    }

    public static class MusicVideos extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "fresku";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "musicVideos"));

            extractor = YouTube.getSearchExtractor(QUERY, singletonList(YoutubeSearchQueryHandlerFactory.MUSIC_VIDEOS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return BASE_SEARCH_URL + QUERY; }
        @Override public String expectedOriginalUrlContains() { return BASE_SEARCH_URL + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
    }

    public static class MusicAlbums extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "johnny sellah";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "musicAlbums"));

            extractor = YouTube.getSearchExtractor(QUERY, singletonList(YoutubeSearchQueryHandlerFactory.MUSIC_ALBUMS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return BASE_SEARCH_URL + URLEncoder.encode(QUERY, StandardCharsets.UTF_8); }
        @Override public String expectedOriginalUrlContains() { return BASE_SEARCH_URL + URLEncoder.encode(QUERY, StandardCharsets.UTF_8); }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.PLAYLIST; }
    }

    public static class MusicPlaylists extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "louivos";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "musicPlaylists"));

            extractor = YouTube.getSearchExtractor(QUERY, singletonList(YoutubeSearchQueryHandlerFactory.MUSIC_PLAYLISTS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return BASE_SEARCH_URL + QUERY; }
        @Override public String expectedOriginalUrlContains() { return BASE_SEARCH_URL + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.PLAYLIST; }
    }

    public static class MusicArtists extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "kevin";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "musicArtists"));

            extractor = YouTube.getSearchExtractor(QUERY, singletonList(YoutubeSearchQueryHandlerFactory.MUSIC_ARTISTS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return BASE_SEARCH_URL + QUERY; }
        @Override public String expectedOriginalUrlContains() { return BASE_SEARCH_URL + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.CHANNEL; }
    }

    @Disabled("2025-07 - backend no longer seems to return any suggestion. "
        + "See YoutubeMusicSearchExtractor#getSearchSuggestion")
    public static class Suggestion extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "megaman x3";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "suggestion"));

            extractor = YouTube.getSearchExtractor(QUERY, singletonList(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return BASE_SEARCH_URL + URLEncoder.encode(QUERY, StandardCharsets.UTF_8); }
        @Override public String expectedOriginalUrlContains() { return BASE_SEARCH_URL + URLEncoder.encode(QUERY, StandardCharsets.UTF_8); }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return "mega man x3"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
    }

    public static class CorrectedSearch extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "no copyrigh sounds";
        private static final String EXPECTED_SUGGESTION = "no copyright sounds";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "corrected"));

            extractor = YouTube.getSearchExtractor(QUERY, singletonList(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return BASE_SEARCH_URL + URLEncoder.encode(QUERY, StandardCharsets.UTF_8); }
        @Override public String expectedOriginalUrlContains() { return BASE_SEARCH_URL + URLEncoder.encode(QUERY, StandardCharsets.UTF_8); }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return EXPECTED_SUGGESTION; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
        @Override public boolean isCorrectedSearch() { return true; }
    }
}
