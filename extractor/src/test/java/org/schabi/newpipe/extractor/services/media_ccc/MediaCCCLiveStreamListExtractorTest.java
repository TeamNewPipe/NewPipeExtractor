package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

public class MediaCCCLiveStreamListExtractorTest {
    private static KioskExtractor extractor;

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = MediaCCC.getKioskList().getExtractorById("live", null);
        extractor.fetchPage();
    }


    @Test
    public void getConferencesListTest() throws Exception {
        final List<InfoItem> a = extractor.getInitialPage().getItems();
        assertTrue(a.size() > 2);
        for (int i = 0; i < a.size(); i++) {
            final InfoItem b = a.get(i);
            assertNotNull(a.get(i).getName());
            assertTrue(a.get(i).getName().length() >= 1);
        }
    }

}
