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
    public static class YoutubeChannelViewCountTest extends YoutubeSearchExtractorBaseTest {
        @BeforeClass
        public static void setUpClass() throws Exception {
            NewPipe.init(Downloader.getInstance(), new Localization("GB", "en"));
            extractor = (YoutubeSearchExtractor) YouTube.getSearchExtractor("pewdiepie",
                    singletonList(YoutubeSearchQueryHandlerFactory.CHANNELS), null, new Localization("GB", "en"));
            extractor.fetchPage();
            itemsPage = extractor.getInitialPage();
        }

        @Test
        public void testViewCount() {
            ChannelInfoItem ci = (ChannelInfoItem) itemsPage.getItems().get(0);
            assertTrue("Count does not fit: " + Long.toString(ci.getSubscriberCount()),
                    69043316 < ci.getSubscriberCount() && ci.getSubscriberCount() < 103043316);
        }
    }
}
