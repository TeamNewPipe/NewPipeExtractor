package org.schabi.newpipe.extractor.services.youtube.search;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory;

import java.net.URLEncoder;

import javax.annotation.Nullable;

import static java.util.Collections.singletonList;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class YoutubeMusicSearchExtractorTest {
    public static class MusicSongs extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "mocromaniac";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "music.youtube.com/search?q=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "music.youtube.com/search?q=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
    }

    public static class MusicVideos extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "fresku";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(YoutubeSearchQueryHandlerFactory.MUSIC_VIDEOS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "music.youtube.com/search?q=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "music.youtube.com/search?q=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
    }

    public static class MusicAlbums extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "johnny sellah";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(YoutubeSearchQueryHandlerFactory.MUSIC_ALBUMS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "music.youtube.com/search?q=" + URLEncoder.encode(QUERY); }
        @Override public String expectedOriginalUrlContains() { return "music.youtube.com/search?q=" + URLEncoder.encode(QUERY); }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.PLAYLIST; }
    }

    public static class MusicPlaylists extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "louivos";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(YoutubeSearchQueryHandlerFactory.MUSIC_PLAYLISTS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "music.youtube.com/search?q=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "music.youtube.com/search?q=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.PLAYLIST; }
    }

    @Ignore
    public static class MusicArtists extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "kevin";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(YoutubeSearchQueryHandlerFactory.MUSIC_ARTISTS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "music.youtube.com/search?q=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "music.youtube.com/search?q=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.CHANNEL; }
    }

    public static class Suggestion extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "megaman x3";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "music.youtube.com/search?q=" + URLEncoder.encode(QUERY); }
        @Override public String expectedOriginalUrlContains() { return "music.youtube.com/search?q=" + URLEncoder.encode(QUERY); }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return "mega man x3"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
    }

    public static class CorrectedSearch extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "duo lipa";
        private static final String EXPECTED_SUGGESTION = "dua lipa";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "music.youtube.com/search?q=" + URLEncoder.encode(QUERY); }
        @Override public String expectedOriginalUrlContains() { return "music.youtube.com/search?q=" + URLEncoder.encode(QUERY); }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return EXPECTED_SUGGESTION; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
        @Override public boolean isCorrectedSearch() { return true; }
    }
}
