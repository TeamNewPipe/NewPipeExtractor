package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.Before;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

/**
 * Test for {@link PlaylistExtractor}
 */

public class SoundcloudPlaylistExtractorTest {
    private PlaylistExtractor extractor;

    @Before
    public void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        extractor = SoundCloud.getService()
                .getPlaylistExtractor("https://soundcloud.com/liluzivert/sets/the-perfect-luv-tape-r");
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
        assertEquals(extractor.getThumbnailUrl(), "https://i1.sndcdn.com/artworks-000174203688-bweu12-large.jpg");
    }

    @Test
    public void testGetUploaderUrl() throws Exception {
        assertEquals(extractor.getUploaderUrl(), "http://soundcloud.com/liluzivert");
    }

    @Test
    public void testGetUploaderName() throws Exception {
        assertEquals(extractor.getUploaderName(), "LIL UZI VERT");
    }

    @Test
    public void testGetUploaderAvatarUrl() throws Exception {
        assertEquals(extractor.getUploaderAvatarUrl(), "https://a1.sndcdn.com/images/default_avatar_large.png");
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
        assertTrue("extractor didn't have more streams", !extractor.hasMoreStreams());
    }
}
