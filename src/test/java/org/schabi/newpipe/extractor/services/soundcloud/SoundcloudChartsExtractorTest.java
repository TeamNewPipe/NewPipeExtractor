package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;

import java.util.List;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

/**
 * Test for {@link SoundcloudChartsUrlIdHandler}
 */
public class SoundcloudChartsExtractorTest {

    static KioskExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        extractor = SoundCloud
                .getKioskList()
                .getExtractorById("Top 50", null);
        extractor.fetchPage();
    }

    @Test
    public void testGetDownloader() throws Exception {
        assertNotNull(NewPipe.getDownloader());
    }

    @Ignore
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
        InfoItemsCollector collector = extractor.getInfoItems();
        if(!collector.getErrors().isEmpty()) {
            System.err.println("----------");
            List<Throwable> errors = collector.getErrors();
            for(Throwable e: errors) {
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
        assertTrue("errors during stream list extraction", extractor.getInfoItems().getErrors().isEmpty());
    }

    @Test
    public void testHasMoreStreams() throws Exception {
        // Setup the streams
        extractor.getInfoItems();
        assertTrue("has more streams", extractor.hasNextPage());
    }

    @Test
    public void testGetNextStreams() throws Exception {
        extractor.getInfoItems();
        assertFalse("extractor has next streams", extractor.getPage(extractor.getNextPageUrl()) == null
                || extractor.getPage(extractor.getNextPageUrl()).infoItemList.isEmpty());
    }

    @Test
    public void testGetCleanUrl() throws Exception {
        assertEquals(extractor.getCleanUrl(), "https://soundcloud.com/charts/top");
    }
}
