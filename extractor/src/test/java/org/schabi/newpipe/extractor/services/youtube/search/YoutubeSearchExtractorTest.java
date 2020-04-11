package org.schabi.newpipe.extractor.services.youtube.search;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest;

import javax.annotation.Nullable;

import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmptyErrors;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.assertNoDuplicatedItems;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.*;

public class YoutubeSearchExtractorTest {
    public static class All extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "test";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getSearchExtractor(QUERY);
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
    }

    public static class Channel extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "test";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(CHANNELS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }

        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.CHANNEL; }
    }

    public static class Playlists extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "test";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(PLAYLISTS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }

        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.PLAYLIST; }
    }

    public static class Videos extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "test";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(VIDEOS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }

        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
    }

    public static class Suggestion extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "newpip";
        private static final String EXPECTED_SUGGESTION = "newpipe";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(VIDEOS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return EXPECTED_SUGGESTION; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
    }

    public static class CorrectedSearch extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "pewdeipie";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(VIDEOS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
        @Override public boolean isCorrectedSearch() { return true; }
    }

    public static class RandomQueryNoMorePages extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "UCO6AK";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getSearchExtractor(QUERY);
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }

        /*//////////////////////////////////////////////////////////////////////////
        // Test Overrides
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testMoreRelatedItems() throws Exception {
            // YouTube actually gives us an empty next page, but after that, no more pages.
            assertTrue(extractor.hasNextPage());
            final ListExtractor.InfoItemsPage<InfoItem> nextEmptyPage = extractor.getPage(extractor.getNextPageUrl());
            assertEquals(0, nextEmptyPage.getItems().size());
            assertEmptyErrors("Empty page has errors", nextEmptyPage.getErrors());

            assertFalse("More items available when it shouldn't", nextEmptyPage.hasNextPage());
            final String nextPageUrl = nextEmptyPage.getNextPageUrl();
            assertTrue("Next page is not empty or null", nextPageUrl == null || nextPageUrl.isEmpty());
        }
    }

    public static class PagingTest {
        @Test
        public void duplicatedItemsCheck() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            final SearchExtractor extractor = YouTube.getSearchExtractor("cirque du soleil", singletonList(VIDEOS), "");
            extractor.fetchPage();

            final ListExtractor.InfoItemsPage<InfoItem> page1 = extractor.getInitialPage();
            final ListExtractor.InfoItemsPage<InfoItem> page2 = extractor.getPage(page1.getNextPageUrl());

            assertNoDuplicatedItems(YouTube, page1, page2);
        }
    }
}
