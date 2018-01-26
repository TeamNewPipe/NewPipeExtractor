package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.List;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmptyErrors;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

/**
 * Test for {@link YoutubePlaylistExtractor}
 */
public class YoutubePlaylistExtractorTest {
    private static YoutubePlaylistExtractor extractor;

    private static void assertNotEmpty(String message, String value) {
        assertNotNull(message, value);
        assertFalse(message, value.isEmpty());
    }

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        extractor = (YoutubePlaylistExtractor) YouTube.getService()
                .getPlaylistExtractor("https://www.youtube.com/watch?v=lp-EO5I60KA&list=PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj");
        extractor.fetchPage();
    }

    @Test
    public void testGetDownloader() throws Exception {
        assertNotNull(NewPipe.getDownloader());
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals(extractor.getId(), "PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj");
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals(extractor.getName(), "Pop Music Playlist: Timeless Pop Hits (Updated Weekly 2018)");
    }

    @Test
    public void testGetThumbnailUrl() throws Exception {
        assertTrue(extractor.getThumbnailUrl(), extractor.getThumbnailUrl().contains("yt"));
    }

    @Test
    public void testGetBannerUrl() throws Exception {
        assertTrue(extractor.getBannerUrl(), extractor.getBannerUrl().contains("yt"));
    }

    @Test
    public void testGetUploaderUrl() throws Exception {
        assertTrue(extractor.getUploaderUrl(), extractor.getUploaderUrl().contains("youtube.com"));
    }

    @Test
    public void testGetUploaderName() throws Exception {
        assertTrue(extractor.getUploaderName(), !extractor.getUploaderName().isEmpty());
    }

    @Test
    public void testGetUploaderAvatarUrl() throws Exception {
        assertTrue(extractor.getUploaderAvatarUrl(), extractor.getUploaderAvatarUrl().contains("yt"));
    }

    @Test
    public void testGetStreamsCount() throws Exception {
        assertTrue("error in the streams count", extractor.getStreamCount() > 100);
    }

    @Test
    public void testGetStreams() throws Exception {
        List<StreamInfoItem> streams = extractor.getStreams().getItemList();
        assertFalse("no streams are received", streams.isEmpty());
        assertTrue(streams.size() > 60);
        assertFalse(streams.contains(null));
        for(StreamInfoItem item: streams) {
            assertEquals("Service id doesn't match", YouTube.getId(), item.getServiceId());
            assertNotNull("Stream type not set: " + item, item.getStreamType());
            //assertNotEmpty("Upload date not set: " + item, item.getTextualUploadDate());
            assertNotEmpty("Uploader name not set: " + item, item.getUploaderName());
            assertNotEmpty("Uploader url not set: " + item, item.getUploaderUrl());
        }
    }

    @Test
    public void testGetStreamsErrors() throws Exception {
        assertEmptyErrors("errors during stream list extraction", extractor.getStreams().getErrors());
    }

    @Test
    public void testHasMoreStreams() throws Exception {
        // Setup the streams
        extractor.getStreams();
        assertTrue("extractor didn't have more streams", extractor.hasMoreStreams());
    }


    @Test @Ignore
    public void testGetNextStreams() throws Exception {
        // Setup the streams
        extractor.getStreams();
        ListExtractor.NextItemsResult nextItemsResult = extractor.getNextStreams();
        assertTrue("extractor didn't have next streams", !nextItemsResult.nextItemsList.isEmpty());
        assertEmptyErrors("errors occurred during extraction of the next streams", nextItemsResult.errors);
        assertTrue("extractor didn't have more streams after getNextStreams", extractor.hasMoreStreams());
    }

}
