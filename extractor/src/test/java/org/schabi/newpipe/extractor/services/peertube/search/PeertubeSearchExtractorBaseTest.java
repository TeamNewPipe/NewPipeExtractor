package org.schabi.newpipe.extractor.services.peertube.search;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeSearchExtractor;

/**
 * Test for {@link PeertubeSearchExtractor}
 */
public abstract class PeertubeSearchExtractorBaseTest {

    protected static PeertubeSearchExtractor extractor;
    protected static ListExtractor.InfoItemsPage<InfoItem> itemsPage;

    @Test
    public void testResultListElementsLength() {
        assertTrue(Integer.toString(itemsPage.getItems().size()),
                itemsPage.getItems().size() >= 3);
    }

    @Test
    public void testUrl() throws Exception {
        assertTrue(extractor.getUrl(), extractor.getUrl().startsWith("https://peertube.mastodon.host/api/v1/search/videos"));
    }
}
