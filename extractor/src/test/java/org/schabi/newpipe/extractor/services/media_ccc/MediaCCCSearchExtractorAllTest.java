package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCSearchExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCSearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

/**
 * Test for {@link MediaCCCSearchExtractor}
 */
public class MediaCCCSearchExtractorAllTest {

    private static SearchExtractor extractor;
    private static ListExtractor.InfoItemsPage<InfoItem> itemsPage;

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor =  MediaCCC.getSearchExtractor( new MediaCCCSearchQueryHandlerFactory()
                .fromQuery("c3", Arrays.asList(new String[0]), ""));
        extractor.fetchPage();
        itemsPage = extractor.getInitialPage();
    }

    @Test
    public void testIfChannelInfoItemsAvailable() {
        boolean isAvialable = false;
        for(InfoItem item : itemsPage.getItems()) {
            if(item instanceof ChannelInfoItem) {
                isAvialable = true;
            }
        }
        assertTrue("ChannelInfoItem not in all list", isAvialable);
    }

    @Test
    public void testIfStreamInfoitemsAvailable() {
        boolean isAvialable = false;
        for(InfoItem item : itemsPage.getItems()) {
            if(item instanceof StreamInfoItem) {
                isAvialable = true;
            }
        }
        assertTrue("ChannelInfoItem not in all list", isAvialable);
    }
}
