// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.Extractor;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampPlaylistInfoItemExtractor;
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
    public void testStreamSearch() throws ExtractionException, IOException {
        SearchExtractor extractor = bandcamp.getSearchExtractor("best friend's basement");

        ListExtractor.InfoItemsPage<InfoItem> page = extractor.getInitialPage();
        InfoItem bestFriendsBasement = page.getItems().get(0);

        // The track by Zach Benson should be the first result, no?
        assertEquals("Best Friend's Basement", bestFriendsBasement.getName());
        assertTrue(bestFriendsBasement.getThumbnailUrl().endsWith(".jpg"));
        assertTrue(bestFriendsBasement.getThumbnailUrl().contains("f4.bcbits.com/img/"));
        assertEquals(InfoItem.InfoType.STREAM, bestFriendsBasement.getInfoType());
    }

    /**
     * Tests whether searching bandcamp for "C418" returns the artist's profile
     */
    @Test
    public void testChannelSearch() throws ExtractionException, IOException {
        SearchExtractor extractor = bandcamp.getSearchExtractor("C418");
        InfoItem c418 = extractor.getInitialPage()
                .getItems().get(0);

        // C418's artist profile should be the first result, no?
        assertEquals("C418", c418.getName());
        assertTrue(c418.getThumbnailUrl().endsWith(".jpg"));
        assertTrue(c418.getThumbnailUrl().contains("f4.bcbits.com/img/"));
        assertEquals("https://c418.bandcamp.com", c418.getUrl());

    }

    /**
     * Tests whether searching bandcamp for "minecraft volume alpha" returns the corresponding album
     */
    @Test
    public void testAlbumSearch() throws ExtractionException, IOException {
        SearchExtractor extractor = bandcamp.getSearchExtractor("minecraft volume alpha");
        InfoItem minecraft = extractor.getInitialPage()
                .getItems().get(0);

        // Minecraft volume alpha should be the first result, no?
        assertEquals("Minecraft - Volume Alpha", minecraft.getName());
        assertTrue(minecraft.getThumbnailUrl().endsWith(".jpg"));
        assertTrue(minecraft.getThumbnailUrl().contains("f4.bcbits.com/img/"));
        assertEquals("https://c418.bandcamp.com/album/minecraft-volume-alpha", minecraft.getUrl());

        // Verify that playlists get counted correctly
        assertEquals(24, ((PlaylistInfoItem) minecraft).getStreamCount());

    }

    /**
     * Tests searches with multiple pages
     */
    @Test
    public void testMultiplePages() throws ExtractionException, IOException {
        // A query practically guaranteed to have the maximum amount of pages
        SearchExtractor extractor = bandcamp.getSearchExtractor("e");

        assertEquals("https://bandcamp.com/search?q=e&page=2", extractor.getInitialPage().getNextPageUrl());

        assertEquals("https://bandcamp.com/search?q=e&page=3", extractor.getPage(extractor.getNextPageUrl()).getNextPageUrl());
    }
}
