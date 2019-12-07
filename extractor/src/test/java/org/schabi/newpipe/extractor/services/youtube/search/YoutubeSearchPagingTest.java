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
    private static int pageSize;

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());

        YoutubeSearchExtractor extractor = (YoutubeSearchExtractor) YouTube.getSearchExtractor("cirque du soleil",
                singletonList(YoutubeSearchQueryHandlerFactory.VIDEOS), null);

        extractor.fetchPage();
        page1 = extractor.getInitialPage();
        urlList1 = extractUrls(page1.getItems());
        assertTrue("page with items loaded", 15 < page1.getItems().size());
        pageSize = page1.getItems().size();
        assertEquals("they are all distinct, no repetition", pageSize, urlList1.size());

        assertTrue("has more than one page of results", page1.hasNextPage());
        assertNotNull("has next page url", page1.getNextPageUrl());
        page2 = extractor.getPage(page1.getNextPageUrl());
        urlList2 = extractUrls(page2.getItems());
    }

    private static Set<String> extractUrls(List<InfoItem> list) {
        Set<String> result = new HashSet<>();
        for (InfoItem item : list) {
            result.add(item.getUrl());
        }
        return result;
    }

    @Test
    public void firstPageOk() {
        assertTrue("first page contains the expected number of items", 15 < page1.getItems().size());
        assertEquals("they are all distinct, no repetition", pageSize, urlList1.size());
    }

    @Test
    public void secondPageLength() {
        assertEquals("second page contains only the expected number of items", pageSize, page2.getItems().size());
    }

    @Test
    public void secondPageUniqueVideos() {
        assertEquals("they are all distinct, no repetition", pageSize, urlList2.size());
    }

    @Test
    public void noRepeatingVideosInPages() {
        Set<String> intersection = new HashSet<>(urlList2);
        intersection.retainAll(urlList1);
        assertEquals("Found a duplicated video on second search page", 0, intersection.size());
    }

}