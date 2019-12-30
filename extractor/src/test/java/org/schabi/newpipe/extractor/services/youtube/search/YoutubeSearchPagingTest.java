package org.schabi.newpipe.extractor.services.youtube.search;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory;

public class YoutubeSearchPagingTest {
    private static ListExtractor.InfoItemsPage<InfoItem> page1;
    private static ListExtractor.InfoItemsPage<InfoItem> page2;
    private static Set<String> urlList1;
    private static Set<String> urlList2;
    private static int page1Size;
    private static int page2Size;

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());

        YoutubeSearchExtractor extractor = (YoutubeSearchExtractor) YouTube.getSearchExtractor("cirque du soleil",
                singletonList(YoutubeSearchQueryHandlerFactory.VIDEOS), null);

        extractor.fetchPage();
        page1 = extractor.getInitialPage();
        urlList1 = extractUrls(page1.getItems());
        assertTrue("failed to load search result page one: too few items", 15 < page1.getItems().size());
        page1Size = page1.getItems().size();
        assertEquals("duplicated items in search result on page one", page1Size, urlList1.size());

        assertTrue("search result has no second page", page1.hasNextPage());
        assertNotNull("next page url is null", page1.getNextPageUrl());
        page2 = extractor.getPage(page1.getNextPageUrl());
        urlList2 = extractUrls(page2.getItems());
        page2Size = page2.getItems().size();
    }

    private static Set<String> extractUrls(List<InfoItem> list) {
        Set<String> result = new HashSet<>();
        for (InfoItem item : list) {
            result.add(item.getUrl());
        }
        return result;
    }

    @Test
    public void secondPageUniqueVideos() {
        assertEquals("Second search result page has duplicated items", page2Size, urlList2.size());
    }

    @Test
    public void noRepeatingVideosInPages() {
        Set<String> intersection = new HashSet<>(urlList2);
        intersection.retainAll(urlList1);
        assertEquals("Found the same item on first AND second search page", 0, intersection.size());
    }

}