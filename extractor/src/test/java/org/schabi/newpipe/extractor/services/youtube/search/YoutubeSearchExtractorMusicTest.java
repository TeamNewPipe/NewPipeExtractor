package org.schabi.newpipe.extractor.services.youtube.search;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory;

import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class YoutubeSearchExtractorMusicTest extends YoutubeSearchExtractorBaseTest {
    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (YoutubeSearchExtractor) YouTube.getSearchExtractor("mocromaniac",
                asList(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS), null);
        extractor.fetchPage();
        itemsPage = extractor.getInitialPage();
    }

    @Test
    public void testGetSecondPage() throws Exception {
        YoutubeSearchExtractor secondExtractor = (YoutubeSearchExtractor) YouTube.getSearchExtractor("mocromaniac",
                asList(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS), null);
        ListExtractor.InfoItemsPage<InfoItem> secondPage = secondExtractor.getPage(itemsPage.getNextPageUrl());
        assertTrue(Integer.toString(secondPage.getItems().size()),
                secondPage.getItems().size() > 10);

        // check if its the same result
        boolean equals = true;
        for (int i = 0; i < secondPage.getItems().size()
                && i < itemsPage.getItems().size(); i++) {
            if (!secondPage.getItems().get(i).getUrl().equals(
                    itemsPage.getItems().get(i).getUrl())) {
                equals = false;
            }
        }
        assertFalse("First and second page are equal", equals);
    }

    @Override
    @Test
    public void testUrl() throws Exception {
        assertTrue(extractor.getUrl(), extractor.getUrl().startsWith("https://music.youtube.com/search?q="));
    }

    @Test
    public void testGetSecondPageUrl() throws Exception {
        URL url = new URL(extractor.getNextPageUrl());

        assertEquals(url.getHost(), "music.youtube.com");
        assertEquals(url.getPath(), "/youtubei/v1/search");

        Map<String, String> queryPairs = new LinkedHashMap<>();
        for (String queryPair : url.getQuery().split("&")) {
            int index = queryPair.indexOf("=");
            queryPairs.put(URLDecoder.decode(queryPair.substring(0, index), "UTF-8"),
                    URLDecoder.decode(queryPair.substring(index + 1), "UTF-8"));
        }

        assertEquals(queryPairs.get("ctoken"), queryPairs.get("continuation"));
        assertTrue(queryPairs.get("continuation").length() > 5);
        assertTrue(queryPairs.get("itct").length() > 5);
        assertEquals("json", queryPairs.get("alt"));
        assertTrue(queryPairs.get("key").length() > 5);
    }
}
