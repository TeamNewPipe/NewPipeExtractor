package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.utils.Localization;
import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

public class JedenTagEinSetKioskTest {
    static KioskExtractor extractor;
    static ListExtractor.InfoItemsPage<StreamInfoItem> initPage;
    static ListExtractor.InfoItemsPage<StreamInfoItem> secondPage;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance(), new Localization("GB", "en"));
        extractor = SoundCloud
                .getKioskList()
                .getExtractorById("jedentageinset", null);
        extractor.fetchPage();
        initPage = extractor.getInitialPage();
        secondPage = extractor.getPage("https://www.jedentageinset.de/page/2");
    }

    @Test
    public void getUrlFromApiUrl() throws Exception {
        assertEquals("https://soundcloud.com/egpodcast/eg744-tiefschwarz",
            SoundcloudParsingHelper.resolveUrlWithEmbedPlayer("https://api.soundcloud.com/tracks/651375380"));
    }

    @Test
    public void getFirstPageSize() throws Exception {
        assertEquals(6, initPage.getItems().size());
    }

    @Test
    public void getName() {
        assertFalse(initPage.getItems().get(0).getName(),
                initPage.getItems().get(0).getName().isEmpty());
    }

    @Test
    public void getUrl() {
        for(StreamInfoItem si : initPage.getItems()) {
            assertTrue(si.getUrl(),
                    si.getUrl().startsWith("https"));
            assertTrue(si.getUrl(),
                    si.getUrl().contains("soundcloud"));
        }
    }

    @Test
    public void getThumbnail() {
        for(StreamInfoItem si : initPage.getItems()) {
            assertTrue(si.getThumbnailUrl(),
                    si.getThumbnailUrl().startsWith("https://www.jedentageinset.de"));
            assertTrue(si.getThumbnailUrl(),
                    si.getThumbnailUrl().endsWith("jpg"));
        }
    }

    @Test
    public void getUploadDate() {
        for(StreamInfoItem si : initPage.getItems()) {
            assertFalse(si.getUploadDate(), si.getUploadDate().isEmpty());
        }
    }

    @Test
    public void testGetNextPageUrl() {
        assertEquals("https://www.jedentageinset.de/page/2",
                initPage.getNextPageUrl());
    }

    @Test
    public void testForErrors() {
        assertTrue(initPage.getErrors().toString(), initPage.getErrors().size() == 0);
    }

    @Test
    public void testSecondPageEqualsFirstPage() {
        for(int i = 0; i < secondPage.getItems().size(); i++) {
            assertFalse("items from first page seem to exist in second: " +
                            initPage.getItems().get(i).getUrl()
                    , initPage.getItems().get(i).getUrl()
                            .equals(secondPage.getItems().get(i).getUrl()));
        }
    }

    @Test
    public void testSecondPageItemCount() {
        assertEquals(6, secondPage.getItems().size());
    }

    @Test
    public void testSecondPageHasErrors() {
        if(!secondPage.getErrors().isEmpty()) {
            for(Throwable e : secondPage.getErrors()) {
                e.printStackTrace();
                System.err.println("---------------");
            }
        }
    }

    @Test
    public void testThirdPageUrl() {
        assertEquals("https://www.jedentageinset.de/page/3", secondPage.getNextPageUrl());
    }
}
