package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCSearchExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCSearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.utils.Localization;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

/**
 * Test for {@link MediaCCCSearchExtractor}
 */
public class MediaCCCSearchExtractorEventsTest {
    private static SearchExtractor extractor;
    private static ListExtractor.InfoItemsPage<InfoItem> itemsPage;

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(Downloader.getInstance(), new Localization("GB", "en"));
        extractor =  MediaCCC.getSearchExtractor( new MediaCCCSearchQueryHandlerFactory()
                .fromQuery("linux", Arrays.asList(new String[] {"events"}), "")
                ,new Localization("GB", "en"));
        extractor.fetchPage();
        itemsPage = extractor.getInitialPage();
    }

    @Test
    public void testCount() throws Exception {
        assertTrue(Integer.toString(itemsPage.getItems().size()),
                itemsPage.getItems().size() >= 25);
    }

    @Test
    public void testServiceId() throws Exception {
        assertEquals(2, extractor.getServiceId());
    }

    @Test
    public void testName() throws Exception {
        assertFalse(itemsPage.getItems().get(0).getName(), itemsPage.getItems().get(0).getName().isEmpty());
    }

    @Test
    public void testUrl() throws Exception {
        assertTrue("Url should start with: https://api.media.ccc.de/public/events/",
                itemsPage.getItems().get(0).getUrl().startsWith("https://api.media.ccc.de/public/events/"));
    }

    @Test
    public void testThumbnailUrl() throws Exception {
        assertTrue(itemsPage.getItems().get(0).getThumbnailUrl(),
                itemsPage.getItems().get(0).getThumbnailUrl().startsWith("https://static.media.ccc.de/media/")
                && itemsPage.getItems().get(0).getThumbnailUrl().endsWith(".jpg"));
    }

    @Test
    public void testReturnTypeStream() throws Exception {
        for(InfoItem item : itemsPage.getItems()) {
            assertTrue("Item is not of type StreamInfoItem", item instanceof StreamInfoItem);
        }
    }
}
