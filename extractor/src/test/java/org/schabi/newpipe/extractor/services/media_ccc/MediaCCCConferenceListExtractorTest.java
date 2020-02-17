package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCConferenceKiosk;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;


/**
 * Test {@link MediaCCCConferenceKiosk}
 */
public class MediaCCCConferenceListExtractorTest {

    private static KioskExtractor extractor;

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = MediaCCC.getKioskList().getDefaultKioskExtractor();
        extractor.fetchPage();
    }

    @Test
    public void getConferencesListTest() throws Exception {
        assertTrue("returned list was to small",
                extractor.getInitialPage().getItems().size() >= 174);
    }

    @Test
    public void conferenceTypeTest() throws Exception {
        assertTrue(contains(extractor.getInitialPage().getItems(), "FrOSCon 2016"));
        assertTrue(contains(extractor.getInitialPage().getItems(), "ChaosWest @ 35c3"));
        assertTrue(contains(extractor.getInitialPage().getItems(), "CTreffOS chaOStalks"));
        assertTrue(contains(extractor.getInitialPage().getItems(), "Datenspuren 2015"));
        assertTrue(contains(extractor.getInitialPage().getItems(), "Chaos Singularity 2017"));
        assertTrue(contains(extractor.getInitialPage().getItems(), "SIGINT10"));
        assertTrue(contains(extractor.getInitialPage().getItems(), "Vintage Computing Festival Berlin 2015"));
        assertTrue(contains(extractor.getInitialPage().getItems(), "FIfFKon 2015"));
        assertTrue(contains(extractor.getInitialPage().getItems(), "33C3: trailers"));
        assertTrue(contains(extractor.getInitialPage().getItems(), "Blinkenlights"));
    }

    private boolean contains(List<InfoItem> itemList, String name) {
        for (InfoItem item : itemList) {
            if (item.getName().equals(name))
                return true;
        }
        return false;
    }
}
