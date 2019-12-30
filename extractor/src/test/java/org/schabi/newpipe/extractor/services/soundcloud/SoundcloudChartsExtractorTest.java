package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.List;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

/**
 * Test for {@link SoundcloudChartsLinkHandlerFactory}
 */
public class SoundcloudChartsExtractorTest {

    static KioskExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = SoundCloud
                .getKioskList()
                .getExtractorById("Top 50", null);
        extractor.fetchPage();
    }

    @Test
    public void testGetDownloader() throws Exception {
        assertNotNull(NewPipe.getDownloader());
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals(extractor.getName(), "Top 50");
    }

    @Test
    public void testId() {
        assertEquals(extractor.getId(), "Top 50");
    }

    @Test
    public void testGetStreams() throws Exception {
        ListExtractor.InfoItemsPage<StreamInfoItem> page = extractor.getInitialPage();
        if(!page.getErrors().isEmpty()) {
            System.err.println("----------");
            List<Throwable> errors = page.getErrors();
            for(Throwable e: errors) {
                e.printStackTrace();
                System.err.println("----------");
            }
        }
        assertTrue("no streams are received",
                !page.getItems().isEmpty()
                        && page.getErrors().isEmpty());
    }

    @Test
    public void testGetStreamsErrors() throws Exception {
        assertTrue("errors during stream list extraction", extractor.getInitialPage().getErrors().isEmpty());
    }

    @Test
    public void testHasMoreStreams() throws Exception {
        // Setup the streams
        extractor.getInitialPage();
        assertTrue("has more streams", extractor.hasNextPage());
    }

    @Test
    public void testGetNextPageUrl() throws Exception {
        assertTrue(extractor.hasNextPage());
    }

    @Test
    public void testGetNextPage() throws Exception {
        extractor.getInitialPage().getItems();
        assertFalse("extractor has next streams", extractor.getPage(extractor.getNextPageUrl()) == null
                || extractor.getPage(extractor.getNextPageUrl()).getItems().isEmpty());
    }

    @Test
    public void testGetCleanUrl() throws Exception {
        assertEquals(extractor.getUrl(), "https://soundcloud.com/charts/top");
    }
}
