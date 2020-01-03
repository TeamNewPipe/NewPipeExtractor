// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampPlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.bandcamp;

/**
 * Tests for {@link BandcampPlaylistExtractor}
 */
public class BandcampPlaylistExtractorTest {

    @BeforeClass
    public static void setUp() {
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    /**
     * Test whether playlists contain the correct amount of items
     */
    @Test
    public void testCount() throws ExtractionException, IOException {
        PlaylistExtractor extractor = bandcamp.getPlaylistExtractor("https://macbenson.bandcamp.com/album/coming-of-age");
        extractor.fetchPage();

        assertEquals(5, extractor.getStreamCount());
    }

    /**
     * Tests whether different stream thumbnails (track covers) get loaded correctly
     */
    @Test
    public void testDifferentTrackCovers() throws ExtractionException, IOException {
        PlaylistExtractor extractor = bandcamp.getPlaylistExtractor("https://zachbensonarchive.bandcamp.com/album/results-of-boredom");
        extractor.fetchPage();

        List<StreamInfoItem> l = extractor.getInitialPage().getItems();
        assertEquals(extractor.getThumbnailUrl(), l.get(0).getThumbnailUrl());
        assertNotEquals(extractor.getThumbnailUrl(), l.get(5).getThumbnailUrl());
    }

    /**
     * Tests that no attempt to load every track's cover individually is made
     */
    @Test(timeout = 10000L)
    public void testDifferentTrackCoversDuration() throws ExtractionException, IOException {
        PlaylistExtractor extractor = bandcamp.getPlaylistExtractor("https://infiniteammo.bandcamp.com/album/night-in-the-woods-vol-1-at-the-end-of-everything");
        extractor.fetchPage();

        /* All tracks in this album have the same cover art, but I don't know any albums with more than 10 tracks
         * that has at least one track with a cover art different from the rest.
         */
        List<StreamInfoItem> l = extractor.getInitialPage().getItems();
        assertEquals(extractor.getThumbnailUrl(), l.get(0).getThumbnailUrl());
        assertEquals(extractor.getThumbnailUrl(), l.get(5).getThumbnailUrl());
    }

    /**
     * Test playlists with locked content
     */
    @Test(expected = ContentNotAvailableException.class)
    public void testLockedContent() throws ExtractionException, IOException {
        PlaylistExtractor extractor = bandcamp.getPlaylistExtractor("https://billwurtz.bandcamp.com/album/high-enough");
        extractor.fetchPage();
    }

    /**
     * Test playlist with just one track
     */
    @Test
    public void testSingleStreamPlaylist() throws ExtractionException, IOException {
        PlaylistExtractor extractor = bandcamp.getPlaylistExtractor("https://zachjohnson1.bandcamp.com/album/endless");
        extractor.fetchPage();

        assertEquals(1, extractor.getStreamCount());

    }
}
