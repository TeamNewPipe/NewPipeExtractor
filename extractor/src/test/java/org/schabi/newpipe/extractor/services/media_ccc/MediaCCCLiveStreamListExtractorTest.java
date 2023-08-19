package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.downloader.MockOnly;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCLiveStreamKiosk;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestListOfItems;

public class MediaCCCLiveStreamListExtractorTest {

    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH
            + "services/media.ccc.de/kiosk/live/";
    private static final String LIVE_KIOSK_ID = MediaCCCLiveStreamKiosk.KIOSK_ID;

    /**
     * Test against the media.ccc.de livestream API endpoint
     * and ensure that no exceptions are thrown.
     */
    public static class LiveDataTest {
        private static KioskExtractor extractor;

        @BeforeAll
        public static void setUpClass() throws Exception {
            MediaCCCTestUtils.ensureStateless();
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = MediaCCC.getKioskList().getExtractorById(LIVE_KIOSK_ID, null);
            extractor.fetchPage();
        }

        @Test
        void getConferencesListTest() throws Exception {
            final ListExtractor.InfoItemsPage liveStreamPage = extractor.getInitialPage();
            final List<InfoItem> items = liveStreamPage.getItems();
            if (items.isEmpty()) {
                // defaultTestListOfItems() fails, if items is empty.
                // This can happen if there are no current live streams.
                // In this case, we just check if an exception was thrown
                assertTrue(liveStreamPage.getErrors().isEmpty());
            } else {
                defaultTestListOfItems(MediaCCC, items, liveStreamPage.getErrors());
            }
        }
    }

    /**
     * Test conferences which are available via the API for C3voc internal testing,
     * but not intended to be shown to users.
     */
    @MockOnly("The live stream API returns different data depending on if and what conferences"
            + " are running. The PreparationTest tests a conference which is used "
            + "for internal testing.")
    public static class PreparationTest {
        private static KioskExtractor extractor;

        @BeforeAll
        public static void setUpClass() throws Exception {
            MediaCCCTestUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "preparation"));
            extractor = MediaCCC.getKioskList().getExtractorById(LIVE_KIOSK_ID, null);
            extractor.fetchPage();
        }

        @Test
        void getConferencesListTest() throws Exception {
            // Testing conferences and the corresponding talks should not be extracted.
            assertTrue(extractor.getInitialPage().getItems().isEmpty());
        }
    }

    /**
     * Test a running conference.
     */
    @MockOnly("The live stream API returns different data depending on if and what conferences"
            + " are running. Using mocks to ensure that there are conferences & talks to extract.")
    public static class LiveConferenceTest {
        private static KioskExtractor extractor;

        @BeforeAll
        public static void setUpClass() throws Exception {
            MediaCCCTestUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "running"));
            extractor = MediaCCC.getKioskList().getExtractorById(LIVE_KIOSK_ID, null);
            extractor.fetchPage();
        }

        @Test
        void getConferencesListTest() throws Exception {
            final ListExtractor.InfoItemsPage liveStreamPage = extractor.getInitialPage();
            final List<InfoItem> items = liveStreamPage.getItems();
            assertEquals(6, items.size());
            defaultTestListOfItems(MediaCCC, items, liveStreamPage.getErrors());

        }
    }
}
