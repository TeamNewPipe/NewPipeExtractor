package org.schabi.newpipe.extractor.services.peertube.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.services.peertube.PeertubeInstance;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeSearchExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

/**
 * Test for {@link PeertubeSearchExtractor}
 */
public class PeertubeSearchExtractorDefaultTest extends PeertubeSearchExtractorBaseTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        // setting instance might break test when running in parallel
        PeerTube.setInstance(new PeertubeInstance("https://peertube.mastodon.host", "PeerTube on Mastodon.host"));
        extractor = (PeertubeSearchExtractor) PeerTube.getSearchExtractor("kde");
        extractor.fetchPage();
        itemsPage = extractor.getInitialPage();
    }

    @Test
    public void testGetSecondPageUrl() throws Exception {
        assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=kde&start=12&count=12", extractor.getNextPageUrl());
    }

    @Test
    public void testResultList_FirstElement() {
        InfoItem firstInfoItem = itemsPage.getItems().get(0);
        
        assertTrue("search does not match", firstInfoItem.getName().toLowerCase().contains("kde"));
    }

    @Test
    public void testResultListCheckIfContainsStreamItems() {
        boolean hasStreams = false;
        for(InfoItem item : itemsPage.getItems()) {
            if(item instanceof StreamInfoItem) {
                hasStreams = true;
            }
        }
        assertTrue("Has no InfoItemStreams", hasStreams);
    }

    @Test
    public void testGetSecondPage() throws Exception {
        extractor = (PeertubeSearchExtractor) PeerTube.getSearchExtractor("internet");
        itemsPage = extractor.getInitialPage();
        PeertubeSearchExtractor secondExtractor =
                (PeertubeSearchExtractor) PeerTube.getSearchExtractor("internet");
        ListExtractor.InfoItemsPage<InfoItem> secondPage = secondExtractor.getPage(itemsPage.getNextPageUrl());
        assertTrue(Integer.toString(secondPage.getItems().size()),
                secondPage.getItems().size() >= 10);

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

        assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=internet&start=24&count=12",
                     secondPage.getNextPageUrl());
    }


    @Test
    public void testId() throws Exception {
        assertEquals("kde", extractor.getId());
    }

    @Test
    public void testName() {
        assertEquals("kde", extractor.getName());
    }
}
