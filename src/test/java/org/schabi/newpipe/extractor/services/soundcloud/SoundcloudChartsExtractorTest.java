package org.schabi.newpipe.extractor.services.soundcloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

import org.junit.Before;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.InfoItemCollector;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;

/**
 * Test for {@link SoundcloudChartsUrlIdHandler}
 */
public class SoundcloudChartsExtractorTest {

    KioskExtractor extractor;

    @Before
    public void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        extractor = SoundCloud.getService()
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
    public void testId() throws Exception {
        assertEquals(extractor.getId(), "Top 50");
    }

    @Test
    public void testGetStreams() throws Exception {
        InfoItemCollector collector = extractor.getStreams();
        if(!collector.getErrors().isEmpty()) {
            System.err.println("----------");
            for(Throwable e : collector.getErrors()) {
                e.printStackTrace();
                System.err.println("----------");
            }
        }
        assertTrue("no streams are received",
                !collector.getItemList().isEmpty()
                        && collector.getErrors().isEmpty());
    }

    @Test
    public void testGetStreamsErrors() throws Exception {
        assertTrue("errors during stream list extraction", extractor.getStreams().getErrors().isEmpty());
    }

    @Test
    public void testHasMoreStreams() throws Exception {
        // Setup the streams
        extractor.getStreams();
        assertTrue("has more streams", extractor.hasMoreStreams());
    }

    @Test
    public void testGetNextStreams() throws Exception {
        extractor.getStreams();
        assertFalse("extractor has next streams", extractor.getNextStreams() == null
                || extractor.getNextStreams().nextItemsList.isEmpty());
    }

    @Test
    public void testGetCleanUrl() throws Exception {
        assertEquals(extractor.getCleanUrl(), "https://soundcloud.com/charts/top");
    }
}
