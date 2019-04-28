package org.schabi.newpipe.extractor.services.soundcloud.search;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudSearchExtractor;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudSearchQueryHandlerFactory;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

public class SoundcloudSearchExtractorChannelOnlyTest extends SoundcloudSearchExtractorBaseTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance(), new Localization("de", "DE"));
        extractor = (SoundcloudSearchExtractor) SoundCloud.getSearchExtractor("lill uzi vert",
                asList(SoundcloudSearchQueryHandlerFactory.USERS), null);
        extractor.fetchPage();
        itemsPage = extractor.getInitialPage();
    }

    @Test
    public void testGetSecondPage() throws Exception {
        SoundcloudSearchExtractor secondExtractor = (SoundcloudSearchExtractor) SoundCloud.getSearchExtractor("lill uzi vert",
                asList(SoundcloudSearchQueryHandlerFactory.USERS), null);
        ListExtractor.InfoItemsPage<InfoItem> secondPage = secondExtractor.getPage(itemsPage.getNextPageUrl());
        assertTrue(Integer.toString(secondPage.getItems().size()),
                secondPage.getItems().size() >= 3);

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

        assertEquals("https://api-v2.soundcloud.com/search/users?q=lill+uzi+vert&limit=10&offset=20",
                removeClientId(secondPage.getNextPageUrl()));
    }

    @Test
    public void testGetSecondPageUrl() throws Exception {
        assertEquals("https://api-v2.soundcloud.com/search/users?q=lill+uzi+vert&limit=10&offset=10",
                removeClientId(extractor.getNextPageUrl()));
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
