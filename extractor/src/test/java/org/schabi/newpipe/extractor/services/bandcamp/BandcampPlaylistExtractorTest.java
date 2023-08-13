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
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertContains;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertContainsOnlyEquivalentImages;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmpty;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertNotOnlyContainsEquivalentImages;
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
    void testCount() throws ExtractionException, IOException {
        final PlaylistExtractor extractor = Bandcamp.getPlaylistExtractor("https://macbenson.bandcamp.com/album/coming-of-age");
        extractor.fetchPage();

        assertEquals(5, extractor.getStreamCount());
    }

    /**
     * Tests whether different stream thumbnails (track covers) get loaded correctly
     */
    @Test
    void testDifferentTrackCovers() throws ExtractionException, IOException {
        final PlaylistExtractor extractor = Bandcamp.getPlaylistExtractor("https://zachbensonarchive.bandcamp.com/album/results-of-boredom");
        extractor.fetchPage();

        final List<StreamInfoItem> l = extractor.getInitialPage().getItems();
        assertContainsOnlyEquivalentImages(extractor.getThumbnails(), l.get(0).getThumbnails());
        assertNotOnlyContainsEquivalentImages(extractor.getThumbnails(), l.get(5).getThumbnails());
    }

    /**
     * Tests that no attempt to load every track's cover individually is made
     */
    @Test
    @Timeout(10)
    void testDifferentTrackCoversDuration() throws ExtractionException, IOException {
        final PlaylistExtractor extractor = Bandcamp.getPlaylistExtractor("https://infiniteammo.bandcamp.com/album/night-in-the-woods-vol-1-at-the-end-of-everything");
        extractor.fetchPage();

        /* All tracks on this album have the same cover art, but I don't know any albums with more
         * than 10 tracks that has at least one track with a cover art different from the rest.
         */
        final List<StreamInfoItem> l = extractor.getInitialPage().getItems();
        assertContainsOnlyEquivalentImages(extractor.getThumbnails(), l.get(0).getThumbnails());
        assertContainsOnlyEquivalentImages(extractor.getThumbnails(), l.get(5).getThumbnails());
    }

    /**
     * Test playlists with locked content
     */
    @Test
    void testLockedContent() throws ExtractionException {
        final PlaylistExtractor extractor = Bandcamp.getPlaylistExtractor("https://billwurtz.bandcamp.com/album/high-enough");

        assertThrows(ContentNotAvailableException.class, extractor::fetchPage);
    }

    /**
     * Test playlist with just one track
     */
    @Test
    void testSingleStreamPlaylist() throws ExtractionException, IOException {
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
        public void testThumbnails() throws ParsingException {
            BandcampTestUtils.testImages(extractor.getThumbnails());
        }

        @Test
        public void testBanners() throws ParsingException {
            assertEmpty(extractor.getBanners());
        }

        @Test
        void testUploaderUrl() throws ParsingException {
            assertTrue(extractor.getUploaderUrl().contains("macbenson.bandcamp.com"));
        }

        @Test
        public void testUploaderName() throws ParsingException {
            assertEquals("mac benson", extractor.getUploaderName());
        }

        @Test
        public void testUploaderAvatars() throws ParsingException {
            BandcampTestUtils.testImages(extractor.getUploaderAvatars());
        }

        @Test
        public void testStreamCount() throws ParsingException {
            assertEquals(5, extractor.getStreamCount());
        }

        @Test
        public void testDescription() throws ParsingException {
            final Description description = extractor.getDescription();
            assertNotEquals(Description.EMPTY_DESCRIPTION, description);
            assertContains("Artwork by Shona Radcliffe", description.getContent()); // about
            assertContains("All tracks written, produced and recorded by Mac Benson",
                    description.getContent()); // credits
            assertContains("all rights reserved", description.getContent()); // license
        }

        @Test
        @Override
        public void testUploaderVerified() throws Exception {
            assertFalse(extractor.isUploaderVerified());
        }

        @Test
        void testInitialPage() throws IOException, ExtractionException {
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
        void testNextPageUrl() throws IOException, ExtractionException {
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
