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

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
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

        assertEquals("https://www.youtube.com/results?q=pewdiepie&sp=EgIQAlAU&gl=GB&page=3", secondPage.getNextPageUrl());
    }

    @Test
    public void testGetSecondPageUrl() throws Exception {
        assertEquals("https://www.youtube.com/results?q=pewdiepie&sp=EgIQAlAU&gl=GB&page=2", extractor.getNextPageUrl());
    }

    @Ignore
    @Test
    public void testOnlyContainChannels() {
        for(InfoItem item : itemsPage.getItems()) {
            if (!(item instanceof ChannelInfoItem)) {
                fail("The following item is no channel item: " + item.toString());
            }
        }
    }

    @Test
    public void testChannelUrl() {
        for(InfoItem item : itemsPage.getItems()) {
            if (item instanceof ChannelInfoItem) {
                ChannelInfoItem channel = (ChannelInfoItem) item;

                if (channel.getSubscriberCount() > 5e7) { // the real PewDiePie
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
