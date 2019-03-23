package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCSearchExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCSearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.utils.Localization;

import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

/**
 * Test for {@link MediaCCCSearchExtractor}
 */
public class MediaCCCSearchExtractorConferencesTest {

    private static SearchExtractor extractor;
    private static ListExtractor.InfoItemsPage<InfoItem> itemsPage;

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(Downloader.getInstance(), new Localization("GB", "en"));
        extractor =  MediaCCC.getSearchExtractor( new MediaCCCSearchQueryHandlerFactory()
                        .fromQuery("c3", Arrays.asList(new String[] {"conferences"}), "")
                ,new Localization("GB", "en"));
        extractor.fetchPage();
        itemsPage = extractor.getInitialPage();
    }

    @Test
    public void testReturnTypeChannel() {
        for(InfoItem item : itemsPage.getItems()) {
            assertTrue("Item is not of type channel", item instanceof ChannelInfoItem);
        }
    }

    @Test
    public void testItemCount() {
        assertTrue("Count is to hight: " + itemsPage.getItems().size(), itemsPage.getItems().size() < 127);
        assertTrue("Countis to low: " + itemsPage.getItems().size(), itemsPage.getItems().size() >= 29);
    }
}
