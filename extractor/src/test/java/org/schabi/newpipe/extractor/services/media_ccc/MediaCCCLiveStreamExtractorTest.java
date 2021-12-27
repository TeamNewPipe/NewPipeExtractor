package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfo;

import java.io.IOException;
import java.util.List;

import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

public class MediaCCCLiveStreamExtractorTest {

    private static KioskExtractor liveKiosk;
    private static StreamExtractor extractor;

    private static List<InfoItem> liveItems;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        liveKiosk = MediaCCC.getKioskList().getExtractorById("live", null);
        liveKiosk.fetchPage();
        liveItems = liveKiosk.getInitialPage().getItems();
        Assume.assumeFalse(
                "Received an empty list of live streams. Skipping MediaCCCLiveStreamExtractorTest",
                liveItems.isEmpty());
    }

    @Test
    public void testRequiredStreamInfo() {
        // Try to get the StreamInfo for each live stream.
        // If some required info is not present an exception will be thrown.
        try {
            for (final InfoItem item : liveItems) {
                StreamInfo.getInfo(item.getUrl());
            }
        } catch (ExtractionException | IOException e) {
            e.printStackTrace();
            Assert.fail("An exception was thrown while getting a StreamInfo for a livestream.");
        }
    }

}
