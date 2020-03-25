package org.schabi.newpipe.extractor.services.youtube.search;

import org.junit.BeforeClass;
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

public class YoutubeSearchExtractorMusicTest {
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
}
