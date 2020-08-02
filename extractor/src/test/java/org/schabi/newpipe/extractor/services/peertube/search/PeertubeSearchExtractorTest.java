package org.schabi.newpipe.extractor.services.peertube.search;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest;
import org.schabi.newpipe.extractor.services.peertube.PeertubeInstance;

import javax.annotation.Nullable;

import static java.util.Collections.singletonList;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.assertNoDuplicatedItems;
import static org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeSearchQueryHandlerFactory.VIDEOS;

public class PeertubeSearchExtractorTest {

    public static class All extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "kde";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://peertube.mastodon.host", "PeerTube on Mastodon.host"));
            extractor = PeerTube.getSearchExtractor(QUERY);
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return PeerTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "/search/videos?search=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "/search/videos?search=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
    }

    public static class PagingTest {
        @Test
        public void duplicatedItemsCheck() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            final SearchExtractor extractor = PeerTube.getSearchExtractor("internet", singletonList(VIDEOS), "");
            extractor.fetchPage();

            final InfoItemsPage<InfoItem> page1 = extractor.getInitialPage();
            final InfoItemsPage<InfoItem> page2 = extractor.getPage(page1.getNextPage());

            assertNoDuplicatedItems(PeerTube, page1, page2);
        }
    }
}
