// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampSearchExtractor;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.bandcamp;

/**
 * Test for {@link BandcampSearchExtractor}
 */
public class BandcampSearchExtractorTest {

    private static BandcampSearchExtractor extractor;

    @BeforeClass
    public static void setUp() {
        NewPipe.init(DownloaderTestImpl.getInstance());

    }

    /**
     * Tests whether searching bandcamp for "best friend's basement" returns
     * the accordingly named song by Zach Benson
     */
    @Test
    public void testBestFriendsBasement() throws ExtractionException, IOException {
        extractor = (BandcampSearchExtractor) bandcamp
                .getSearchExtractor("best friend's basement");

        ListExtractor.InfoItemsPage<InfoItem> page = extractor.getInitialPage();
        InfoItem bestFriendsBasement = page.getItems().get(0);

        // The track by Zach Benson should be the first result, no?
        assertEquals("Best Friend's Basement", bestFriendsBasement.getName());
        assertTrue(bestFriendsBasement.getThumbnailUrl().endsWith(".jpg"));
        assertTrue(bestFriendsBasement.getThumbnailUrl().contains("f4.bcbits.com/img/"));
        assertEquals(InfoItem.InfoType.STREAM, bestFriendsBasement.getInfoType());




    }
}
