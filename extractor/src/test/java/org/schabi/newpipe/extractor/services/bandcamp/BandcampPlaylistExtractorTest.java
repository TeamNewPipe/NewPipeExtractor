// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.BasePlaylistExtractorTest;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampPlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

/**
 * Tests for {@link BandcampPlaylistExtractor}
 */
public class BandcampPlaylistExtractorTest {

    @BeforeAll
    public static void setUp() {
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    /**
     * Test whether playlists contain the correct amount of items
     */
    @Test
    public void testCount() throws ExtractionException, IOException {
        final PlaylistExtractor extractor = Bandcamp.getPlaylistExtractor("https://macbenson.bandcamp.com/album/coming-of-age");
        extractor.fetchPage();

        assertEquals(5, extractor.getStreamCount());
    }

    /**
     * Tests whether different stream thumbnails (track covers) get loaded correctly
     */
    @Test
    public void testDifferentTrackCovers() throws ExtractionException, IOException {
        final PlaylistExtractor extractor = Bandcamp.getPlaylistExtractor("https://zachbensonarchive.bandcamp.com/album/results-of-boredom");
        extractor.fetchPage();

        final List<StreamInfoItem> l = extractor.getInitialPage().getItems();
        assertEquals(extractor.getThumbnailUrl(), l.get(0).getThumbnailUrl());
        assertNotEquals(extractor.getThumbnailUrl(), l.get(5).getThumbnailUrl());
    }

    /**
     * Tests that no attempt to load every track's cover individually is made
     */
    @Test
    @Timeout(10)
    public void testDifferentTrackCoversDuration() throws ExtractionException, IOException {
        final PlaylistExtractor extractor = Bandcamp.getPlaylistExtractor("https://infiniteammo.bandcamp.com/album/night-in-the-woods-vol-1-at-the-end-of-everything");
        extractor.fetchPage();

        /* All tracks in this album have the same cover art, but I don't know any albums with more than 10 tracks
         * that has at least one track with a cover art different from the rest.
         */
        final List<StreamInfoItem> l = extractor.getInitialPage().getItems();
        assertEquals(extractor.getThumbnailUrl(), l.get(0).getThumbnailUrl());
        assertEquals(extractor.getThumbnailUrl(), l.get(5).getThumbnailUrl());
    }

    /**
     * Test playlists with locked content
     */
    @Test
    public void testLockedContent() throws ExtractionException, IOException {
        final PlaylistExtractor extractor = Bandcamp.getPlaylistExtractor("https://billwurtz.bandcamp.com/album/high-enough");

        assertThrows(ContentNotAvailableException.class, extractor::fetchPage);
    }

    /**
     * Test playlist with just one track
     */
    @Test
    public void testSingleStreamPlaylist() throws ExtractionException, IOException {
        final PlaylistExtractor extractor = Bandcamp.getPlaylistExtractor("https://zachjohnson1.bandcamp.com/album/endless");
        extractor.fetchPage();

        assertEquals(1, extractor.getStreamCount());

    }

    public static class ComingOfAge implements BasePlaylistExtractorTest {

        private static PlaylistExtractor extractor;

        @BeforeAll
        public static void setUp() throws ExtractionException, IOException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = Bandcamp.getPlaylistExtractor("https://macbenson.bandcamp.com/album/coming-of-age");
            extractor.fetchPage();
        }

        @Test
        public void testThumbnailUrl() throws ParsingException {
            assertTrue(extractor.getThumbnailUrl().contains("f4.bcbits.com/img"));
        }

        @Test
        public void testBannerUrl() throws ParsingException {
            assertEquals("", extractor.getBannerUrl());
        }

        @Test
        public void testUploaderUrl() throws ParsingException {
            assertTrue(extractor.getUploaderUrl().contains("macbenson.bandcamp.com"));
        }

        @Test
        public void testUploaderName() throws ParsingException {
            assertEquals("mac benson", extractor.getUploaderName());
        }

        @Test
        public void testUploaderAvatarUrl() throws ParsingException {
            assertTrue(extractor.getUploaderAvatarUrl().contains("f4.bcbits.com/img"));
        }

        @Test
        public void testStreamCount() throws ParsingException {
            assertEquals(5, extractor.getStreamCount());
        }

        @Override
        public void testUploaderVerified() throws Exception {
            assertFalse(extractor.isUploaderVerified());
        }

        @Test
        public void testInitialPage() throws IOException, ExtractionException {
            assertNotNull(extractor.getInitialPage().getItems().get(0));
        }

        @Test
        public void testServiceId() {
            assertEquals(Bandcamp.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testName() throws ParsingException {
            assertEquals("Coming of Age", extractor.getName());
        }

        @Test
        public void testId() throws Exception {
            assertEquals("https://macbenson.bandcamp.com/album/coming-of-age", extractor.getId());
        }

        @Test
        public void testUrl() throws Exception {
            assertEquals("https://macbenson.bandcamp.com/album/coming-of-age", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws Exception {
            assertEquals("https://macbenson.bandcamp.com/album/coming-of-age", extractor.getOriginalUrl());
        }

        @Test
        public void testNextPageUrl() throws IOException, ExtractionException {
            assertNull(extractor.getPage(extractor.getInitialPage().getNextPage()));
        }

        @Test
        public void testRelatedItems() throws Exception {
            // DefaultTests.defaultTestRelatedItems(extractor);
            // Would fail because BandcampPlaylistStreamInfoItemExtractor.getUploaderName() returns an empty String
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
        }
    }
}
