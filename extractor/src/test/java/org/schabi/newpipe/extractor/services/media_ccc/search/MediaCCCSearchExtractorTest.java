package org.schabi.newpipe.extractor.services.media_ccc.search;

import org.junit.jupiter.api.BeforeAll;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest;
import org.schabi.newpipe.extractor.services.media_ccc.search.filter.MediaCCCFilters;


import javax.annotation.Nullable;

import static java.util.Collections.singletonList;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

public class MediaCCCSearchExtractorTest {
    public static class All extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "kde";

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = MediaCCC.getSearchExtractor(QUERY);
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return MediaCCC; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "media.ccc.de/public/events/search?q=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "media.ccc.de/public/events/search?q=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }

        @Override public boolean expectedHasMoreItems() { return false; }
    }

    public static class Conferences extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "c3";

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = MediaCCC.getSearchExtractor(QUERY, singletonList(
                    getFilterItem(MediaCCC, MediaCCCFilters.ID_CF_MAIN_CONFERENCES)), null);
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return MediaCCC; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "media.ccc.de/public/events/search?q=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "media.ccc.de/public/events/search?q=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }

        @Nullable @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.CHANNEL; }
        @Override public boolean expectedHasMoreItems() { return false; }
    }

    public static class Events extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "linux";

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = MediaCCC.getSearchExtractor(QUERY, singletonList(
                    getFilterItem(MediaCCC, MediaCCCFilters.ID_CF_MAIN_EVENTS)), null);
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return MediaCCC; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "media.ccc.de/public/events/search?q=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "media.ccc.de/public/events/search?q=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }

        @Nullable @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
        @Override public boolean expectedHasMoreItems() { return false; }
    }
}
