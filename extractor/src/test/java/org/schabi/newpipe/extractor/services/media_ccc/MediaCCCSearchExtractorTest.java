package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCSearchExtractor;
import org.schabi.newpipe.extractor.utils.Localization;

import static junit.framework.TestCase.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

/**
 * Test for {@link MediaCCCSearchExtractor}
 */
public class MediaCCCSearchExtractorTest {
    private static SearchExtractor extractor;
    private static ListExtractor.InfoItemsPage<InfoItem> itemsPage;

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(Downloader.getInstance(), new Localization("GB", "en"));
        extractor =  MediaCCC.getSearchExtractor("source");
        extractor.fetchPage();
        itemsPage = extractor.getInitialPage();
    }

    @Test
    public void testCount() throws Exception {
        assertTrue(Integer.toString(itemsPage.getItems().size()),
                itemsPage.getItems().size() >= 25);
    }
}
