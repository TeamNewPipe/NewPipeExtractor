package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class MediaCCCRecentListExtractorTest {
    private static KioskExtractor extractor;

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = MediaCCC.getKioskList().getExtractorById("recent", null);
        extractor.fetchPage();
    }

    @Test
    @Ignore("TODO fix")
    public void testStreamList() throws Exception {
        final List<StreamInfoItem> items = extractor.getInitialPage().getItems();
        assertEquals(100, items.size());
        for (final StreamInfoItem item: items) {
            assertFalse(isNullOrEmpty(item.getName()));
            assertTrue(item.getDuration() > 0);
            assertTrue(isNullOrEmpty(item.getUploaderName())); // we do not get the uploader name
            assertTrue(item.getUploadDate().offsetDateTime().isBefore(OffsetDateTime.now()));
            assertTrue(item.getUploadDate().offsetDateTime().isAfter(OffsetDateTime.now().minusYears(1)));
        }
    }


}
