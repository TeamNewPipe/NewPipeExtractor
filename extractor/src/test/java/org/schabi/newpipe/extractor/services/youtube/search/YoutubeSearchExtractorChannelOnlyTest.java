package org.schabi.newpipe.extractor.services.youtube.search;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.ContentFilter;
import org.schabi.newpipe.extractor.utils.Localization;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class YoutubeSearchExtractorChannelOnlyTest extends YoutubeSearchExtractorBaseTest {

    private static final String SEARCH_QUERY = "pewdiepie";
    private static final Localization DEFAULT_LOCALIZATION = new Localization("GB", "en");

    private static String baseSearchPageURL;

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(Downloader.getInstance(), DEFAULT_LOCALIZATION);
        extractor = (YoutubeSearchExtractor) YouTube.getSearchExtractor(SEARCH_QUERY,
                Collections.singletonList(ContentFilter.channels.name()), null, DEFAULT_LOCALIZATION);
        extractor.fetchPage();
        itemsPage = extractor.getInitialPage();
        baseSearchPageURL = extractor.getUrl();
    }

    @Test
    public void testGetSecondPage() throws Exception {
        YoutubeSearchExtractor secondExtractor = (YoutubeSearchExtractor) YouTube.getSearchExtractor(SEARCH_QUERY,
                Collections.singletonList(ContentFilter.channels.name()), null, DEFAULT_LOCALIZATION);
        ListExtractor.InfoItemsPage<InfoItem> secondPage = secondExtractor.getPage(itemsPage.getNextPageUrl());
        assertTrue(Integer.toString(secondPage.getItems().size()),
                secondPage.getItems().size() > 10);

        // check if its the same result
        boolean equals = true;
        for (int i = 0; i < secondPage.getItems().size()
                && i < itemsPage.getItems().size(); i++) {
            if(!secondPage.getItems().get(i).getUrl().equals(
                    itemsPage.getItems().get(i).getUrl())) {
                equals = false;
            }
        }
        assertFalse("First and second page are equal", equals);

        String thirdSearchPageURL = secondPage.getNextPageUrl();
        assertTrue("Third search page URL contains base search page URL along with page 3 identifier",
                thirdSearchPageURL.contains(baseSearchPageURL) && thirdSearchPageURL.contains("&page=3"));
    }

    @Test
    public void testGetSecondPageUrl() throws Exception {
        String secondSearchPageURL = extractor.getNextPageUrl();
        assertTrue("Second search page URL contains base search page URL along with page 2 identifier",
                secondSearchPageURL.contains(baseSearchPageURL) && secondSearchPageURL.contains("&page=2"));
    }

    @Test
    public void testOnlyContainChannels() {
        for(InfoItem item : itemsPage.getItems()) {
            if(!(item instanceof ChannelInfoItem)) {
                fail("The following item is no channel item: " + item.toString());
            }
        }
    }
}
