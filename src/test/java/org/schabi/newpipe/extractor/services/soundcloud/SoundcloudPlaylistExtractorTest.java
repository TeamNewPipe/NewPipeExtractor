package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

/**
 * Test for {@link PlaylistExtractor}
 */

public class SoundcloudPlaylistExtractorTest {
    private static PlaylistExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        extractor = SoundCloud
                .getPlaylistExtractor("https://soundcloud.com/liluzivert/sets/the-perfect-luv-tape-r");
        extractor.fetchPage();
    }

    @Test
    public void testGetDownloader() throws Exception {
        assertNotNull(NewPipe.getDownloader());
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals(extractor.getId(), "246349810");
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals(extractor.getName(), "THE PERFECT LUV TAPE®️");
    }

    @Test
    public void testGetThumbnailUrl() throws Exception {
        assertIsSecureUrl(extractor.getThumbnailUrl());
    }

    @Test
    public void testGetUploaderUrl() throws Exception {
        assertIsSecureUrl(extractor.getUploaderUrl());
        assertEquals(extractor.getUploaderUrl(), "https://soundcloud.com/liluzivert");
    }

    @Test
    public void testGetUploaderName() throws Exception {
        assertEquals(extractor.getUploaderName(), "LIL UZI VERT");
    }

    @Test
    public void testGetUploaderAvatarUrl() throws Exception {
        assertIsSecureUrl(extractor.getUploaderAvatarUrl());
    }

    @Test
    public void testGetStreamsCount() throws Exception {
        assertEquals(extractor.getStreamCount(), 10);
    }

    @Test
    public void testGetStreams() throws Exception {
        assertTrue("no streams are received", !extractor.getStreams().getItemList().isEmpty());
    }

    @Test
    public void testGetStreamsErrors() throws Exception {
        assertTrue("errors during stream list extraction", extractor.getStreams().getErrors().isEmpty());
    }

    @Test
    public void testHasMoreStreams() throws Exception {
        // Setup the streams
        extractor.getStreams();
        assertTrue("extractor didn't have more streams", !extractor.hasNextPage());
    }

    @Test(expected = ExtractionException.class)
    public void testGetNextPageNonExistent() throws Exception {
        // Setup the streams
        extractor.getStreams();

        // This playlist don't have more streams, it should throw an error
        extractor.getPage(extractor.getNextPageUrl());

        fail("Expected exception wasn't thrown");
    }
}
