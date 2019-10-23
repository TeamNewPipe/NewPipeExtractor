package org.schabi.newpipe.extractor.services.youtube.search;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.utils.Localization;

import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

/**
 * Test for {@link YoutubeSearchExtractor}
 */
public class YoutubeSearchCountTest {
    public static class YoutubeChannelSubscriberCountTest extends YoutubeSearchExtractorBaseTest {
        @BeforeClass
        public static void setUpClass() throws Exception {
            NewPipe.init(Downloader.getInstance(), new Localization("GB", "en"));
            extractor = (YoutubeSearchExtractor) YouTube.getSearchExtractor("pewdiepie",
                    singletonList(YoutubeSearchQueryHandlerFactory.CHANNELS), null, new Localization("GB", "en"));
            extractor.fetchPage();
            itemsPage = extractor.getInitialPage();
        }

        @Test
        public void testSubscriberCount() {
            ChannelInfoItem ci = (ChannelInfoItem) itemsPage.getItems().get(0);
            long subscriberCount = ci.getSubscriberCount();

            // Only test if subscribers count is available in the search page
            if (subscriberCount != -1) {
                assertTrue("Count does not fit: " + Long.toString(subscriberCount),
                        69043316 < subscriberCount && subscriberCount < 103043316);
            }
        }
    }
}
