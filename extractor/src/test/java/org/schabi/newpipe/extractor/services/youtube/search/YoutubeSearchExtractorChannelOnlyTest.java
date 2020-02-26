package org.schabi.newpipe.extractor.services.youtube.search;

import org.hamcrest.CoreMatchers;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory;

import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class YoutubeSearchExtractorChannelOnlyTest extends YoutubeSearchExtractorBaseTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (YoutubeSearchExtractor) YouTube.getSearchExtractor("pewdiepie",
                asList(YoutubeSearchQueryHandlerFactory.CHANNELS), null);
        extractor.fetchPage();
        itemsPage = extractor.getInitialPage();
    }

    @Test
    public void testGetSecondPage() throws Exception {
        YoutubeSearchExtractor secondExtractor = (YoutubeSearchExtractor) YouTube.getSearchExtractor("pewdiepie",
                asList(YoutubeSearchQueryHandlerFactory.CHANNELS), null);
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

    @Test
    public void testGetSecondPageUrl() throws Exception {
        URL url = new URL(extractor.getNextPageUrl());

        assertEquals(url.getHost(), "www.youtube.com");
        assertEquals(url.getPath(), "/results");

        Map<String, String> queryPairs = new LinkedHashMap<>();
        for (String queryPair : url.getQuery().split("&")) {
            int index = queryPair.indexOf("=");
            queryPairs.put(URLDecoder.decode(queryPair.substring(0, index), "UTF-8"),
                    URLDecoder.decode(queryPair.substring(index + 1), "UTF-8"));
        }

        assertEquals("pewdiepie", queryPairs.get("search_query"));
        assertEquals(queryPairs.get("ctoken"), queryPairs.get("continuation"));
        assertTrue(queryPairs.get("continuation").length() > 5);
        assertTrue(queryPairs.get("itct").length() > 5);
    }

    @Ignore
    @Test
    public void testOnlyContainChannels() {
        for (InfoItem item : itemsPage.getItems()) {
            if (!(item instanceof ChannelInfoItem)) {
                fail("The following item is no channel item: " + item.toString());
            }
        }
    }

    @Test
    public void testChannelUrl() {
        for (InfoItem item : itemsPage.getItems()) {
            if (item instanceof ChannelInfoItem) {
                ChannelInfoItem channel = (ChannelInfoItem) item;

                if (channel.getSubscriberCount() > 5e7 && channel.getName().equals("PewDiePie")) { // the real PewDiePie
                    assertEquals("https://www.youtube.com/channel/UC-lHJZR3Gqxm24_Vd_AJ5Yw", item.getUrl());
                } else {
                    assertThat(item.getUrl(), CoreMatchers.startsWith("https://www.youtube.com/channel/"));
                }
            }
        }
    }

    @Test
    public void testStreamCount() {
        ChannelInfoItem ci = (ChannelInfoItem) itemsPage.getItems().get(0);
        assertTrue("Stream count does not fit: " + ci.getStreamCount(),
                4000 < ci.getStreamCount() && ci.getStreamCount() < 5500);
    }
}
