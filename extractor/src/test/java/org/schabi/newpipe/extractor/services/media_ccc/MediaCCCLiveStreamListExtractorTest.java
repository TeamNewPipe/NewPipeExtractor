package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.MockOnly;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCLiveStreamKiosk;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestListOfItems;

public class MediaCCCLiveStreamListExtractorTest extends DefaultSimpleExtractorTest<KioskExtractor> {
    private static final String LIVE_KIOSK_ID = MediaCCCLiveStreamKiosk.KIOSK_ID;

    @Override
    protected KioskExtractor createExtractor() throws Exception {
        return MediaCCC.getKioskList().getExtractorById("live", null);
    }

    @Test
    public void getConferencesListTest() {
        assertDoesNotThrow(() -> extractor().getInitialPage().getItems());
    }

    /**
     * Test against the media.ccc.de livestream API endpoint
     * and ensure that no exceptions are thrown.
     */
    public static class LiveDataTest extends DefaultSimpleExtractorTest<KioskExtractor> {

        @BeforeAll
        @Override
        public void setUp() throws Exception {
            MediaCCCTestUtils.ensureStateless();
            super.setUp();
        }

        @Override
        protected KioskExtractor createExtractor() throws Exception {
            return MediaCCC.getKioskList().getExtractorById(LIVE_KIOSK_ID, null);
        }

        @Test
        void getConferencesListTest() throws Exception {
            final ListExtractor.InfoItemsPage liveStreamPage = extractor().getInitialPage();
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
    public static class PreparationTest extends DefaultSimpleExtractorTest<KioskExtractor> {

        @BeforeAll
        @Override
        public void setUp() throws Exception {
            MediaCCCTestUtils.ensureStateless();
            super.setUp();
        }

        @Override
        protected KioskExtractor createExtractor() throws Exception {
            return MediaCCC.getKioskList().getExtractorById(LIVE_KIOSK_ID, null);
        }

        @Test
        void getConferencesListTest() throws Exception {
            // Testing conferences and the corresponding talks should not be extracted.
            assertTrue(extractor().getInitialPage().getItems().isEmpty());
        }
    }

    /**
     * Test a running conference.
     */
    @MockOnly("The live stream API returns different data depending on if and what conferences"
            + " are running. Using mocks to ensure that there are conferences & talks to extract.")
    public static class LiveConferenceTest extends DefaultSimpleExtractorTest<KioskExtractor> {

        @BeforeAll
        @Override
        public void setUp() throws Exception {
            MediaCCCTestUtils.ensureStateless();
            super.setUp();
        }

        @Override
        protected KioskExtractor createExtractor() throws Exception {
            return MediaCCC.getKioskList().getExtractorById(LIVE_KIOSK_ID, null);
        }

        @Test
        void getConferencesListTest() throws Exception {
            final ListExtractor.InfoItemsPage liveStreamPage = extractor().getInitialPage();
            final List<InfoItem> items = liveStreamPage.getItems();
            assertEquals(6, items.size());
            defaultTestListOfItems(MediaCCC, items, liveStreamPage.getErrors());

        }
    }

}
