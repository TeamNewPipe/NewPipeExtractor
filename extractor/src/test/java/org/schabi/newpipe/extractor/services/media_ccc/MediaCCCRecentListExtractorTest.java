package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class MediaCCCRecentListExtractorTest {
    private static KioskExtractor extractor;

    @BeforeAll
    public static void setUpClass() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = MediaCCC.getKioskList().getExtractorById("recent", null);
        extractor.fetchPage();
    }

    @Test
    public void testStreamList() throws Exception {
        final List<StreamInfoItem> items = extractor.getInitialPage().getItems();
        assertEquals(100, items.size());
        for (final StreamInfoItem item: items) {
            assertFalse(isNullOrEmpty(item.getName()));
            assertTrue(item.getDuration() > 0);
            // Disabled for now, because sometimes videos are uploaded, but their release date is in the future
            // assertTrue(item.getUploadDate().offsetDateTime().isBefore(OffsetDateTime.now()));
        }
    }


}
