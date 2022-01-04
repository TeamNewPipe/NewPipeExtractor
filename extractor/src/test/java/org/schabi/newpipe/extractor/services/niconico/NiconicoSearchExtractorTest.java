package org.schabi.newpipe.extractor.services.niconico;

import org.junit.BeforeClass;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest;

import static org.schabi.newpipe.extractor.ServiceList.Niconico;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;
import java.net.URLEncoder;

import javax.annotation.Nullable;

public class NiconicoSearchExtractorTest {
    public static class Keyword extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "VOCALOID";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = Niconico.getSearchExtractor(QUERY);
            extractor.fetchPage();
        }

        @Override
        public SearchExtractor extractor() throws Exception {
            return extractor;
        }

        @Override
        public StreamingService expectedService() throws Exception {
            return ServiceList.Niconico;
        }

        @Override
        public String expectedName() throws Exception {
            return QUERY;
        }

        @Override
        public String expectedId() throws Exception {
            return QUERY;
        }

        @Override
        public String expectedUrlContains() throws Exception {
            return "https://api.search.nicovideo.jp/api/v2/snapshot/video/contents/search"
                    + "?q="
                    + URLEncoder.encode(QUERY, UTF_8);
        }

        @Override
        public String expectedOriginalUrlContains() throws Exception {
            return "https://api.search.nicovideo.jp/api/v2/snapshot/video/contents/search"
                    + "?q="
                    + URLEncoder.encode(QUERY, UTF_8);
        }

        @Override
        public String expectedSearchString() {
            return QUERY;
        }

        @Nullable
        @Override
        public String expectedSearchSuggestion() {
            return null;
        }
    }
}
