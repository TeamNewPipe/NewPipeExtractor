package org.schabi.newpipe.extractor.services.peertube.search;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest;
import org.schabi.newpipe.extractor.services.peertube.PeertubeInstance;
import org.schabi.newpipe.extractor.services.peertube.search.filter.PeertubeFilters;

import javax.annotation.Nullable;

import static java.util.Collections.singletonList;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.assertNoDuplicatedItems;

public class PeertubeSearchExtractorTest {

    public static class All extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "fsf";

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://framatube.org", "Framatube"));
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

    public static class SepiaSearch extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "kde";

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://framatube.org", "Framatube"));
            final FilterItem item = DefaultSearchExtractorTest.getFilterItem(
                    PeerTube, PeertubeFilters.ID_CF_SEPIA_SEPIASEARCH);
            extractor = PeerTube.getSearchExtractor(QUERY, singletonList(item), null);
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
        @Disabled("Exception in CI: javax.net.ssl.SSLHandshakeException: PKIX path validation failed: java.security.cert.CertPathValidatorException: validity check failed")
        public void duplicatedItemsCheck() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            final FilterItem item = DefaultSearchExtractorTest.getFilterItem(
                    PeerTube, PeertubeFilters.ID_CF_MAIN_VIDEOS);
            final SearchExtractor extractor =
                    PeerTube.getSearchExtractor("internet", singletonList(item), null);
            extractor.fetchPage();

            final InfoItemsPage<InfoItem> page1 = extractor.getInitialPage();
            final InfoItemsPage<InfoItem> page2 = extractor.getPage(page1.getNextPage());

            assertNoDuplicatedItems(PeerTube, page1, page2);
        }
    }
}
