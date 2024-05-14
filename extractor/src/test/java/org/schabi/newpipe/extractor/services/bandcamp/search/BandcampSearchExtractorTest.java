// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest;
import org.schabi.newpipe.extractor.services.bandcamp.BandcampTestUtils;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampSearchExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;

import javax.annotation.Nullable;

/**
 * Test for {@link BandcampSearchExtractor}
 */
public class BandcampSearchExtractorTest {

    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/bandcamp/extractor/search";

    @BeforeAll
    public static void setUp() throws IOException {
        NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH));
    }

    /**
     * Tests whether searching bandcamp for "best friend's basement" returns
     * the accordingly named song by Zach Benson
     */
    @Test
    void testStreamSearch() throws ExtractionException, IOException {
        final SearchExtractor extractor = Bandcamp.getSearchExtractor("best friend's basement");

        final ListExtractor.InfoItemsPage<InfoItem> page = extractor.getInitialPage();
        final StreamInfoItem bestFriendsBasement = (StreamInfoItem) page.getItems().get(0);

        // The track by Zach Benson should be the first result, no?
        assertEquals("Best Friend's Basement", bestFriendsBasement.getName());
        assertEquals("Zach Benson", bestFriendsBasement.getUploaderName());
        BandcampTestUtils.testImages(bestFriendsBasement.getThumbnails());
        assertEquals(InfoItem.InfoType.STREAM, bestFriendsBasement.getInfoType());
    }

    /**
     * Tests whether searching bandcamp for "C418" returns the artist's profile
     */
    @Test
    void testChannelSearch() throws ExtractionException, IOException {
        final SearchExtractor extractor = Bandcamp.getSearchExtractor("C418");
        final InfoItem c418 = extractor.getInitialPage()
                .getItems().get(0);

        // C418's artist profile should be the first result, no?
        assertEquals("C418", c418.getName());
        BandcampTestUtils.testImages(c418.getThumbnails());
        assertEquals("https://c418.bandcamp.com", c418.getUrl());
    }

    /**
     * Tests whether searching bandcamp for "minecraft volume alpha" returns the corresponding album
     */
    @Test
    void testAlbumSearch() throws ExtractionException, IOException {
        final SearchExtractor extractor = Bandcamp.getSearchExtractor("minecraft volume alpha");
        final InfoItem minecraft = extractor.getInitialPage().getItems().get(0);

        // Minecraft volume alpha should be the first result, no?
        assertEquals("Minecraft - Volume Alpha", minecraft.getName());
        BandcampTestUtils.testImages(minecraft.getThumbnails());
        assertEquals(
                "https://c418.bandcamp.com/album/minecraft-volume-alpha",
                minecraft.getUrl());

        // Verify that playlist tracks counts get extracted correctly
        assertEquals(24, ((PlaylistInfoItem) minecraft).getStreamCount());
    }

    /**
     * Tests searches with multiple pages
     */
    @Test
    void testMultiplePages() throws ExtractionException, IOException {
        // A query practically guaranteed to have the maximum amount of pages
        final SearchExtractor extractor = Bandcamp.getSearchExtractor("e");

        final Page page2 = extractor.getInitialPage().getNextPage();
        assertEquals("https://bandcamp.com/search?q=e&page=2", page2.getUrl());

        final Page page3 = extractor.getPage(page2).getNextPage();
        assertEquals("https://bandcamp.com/search?q=e&page=3", page3.getUrl());
    }

    public static class DefaultTest extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "noise";

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "default"));
            extractor = Bandcamp.getSearchExtractor(QUERY);
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return Bandcamp; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "bandcamp.com/search?q=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "bandcamp.com/search?q=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
    }
}
