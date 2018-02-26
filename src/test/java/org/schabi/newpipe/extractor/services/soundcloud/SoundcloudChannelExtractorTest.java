package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

/**
 * Test for {@link ChannelExtractor}
 */

public class SoundcloudChannelExtractorTest {

    static ChannelExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        extractor = SoundCloud
                .getChannelExtractor("https://soundcloud.com/liluzivert");
        extractor.fetchPage();
    }

    @Test
    public void testGetDownloader() throws Exception {
        assertNotNull(NewPipe.getDownloader());
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals("LIL UZI VERT", extractor.getName());
    }

    @Test
    public void testGetDescription() throws Exception {
        assertTrue(extractor.getDescription() != null);
    }

    @Test
    public void testGetAvatarUrl() throws Exception {
        assertIsSecureUrl(extractor.getAvatarUrl());
    }

    @Test
    public void testGetStreams() throws Exception {
        assertFalse("no streams are received", extractor.getStreams().getItemList().isEmpty());
    }

    @Test
    public void testGetStreamsErrors() throws Exception {
        assertTrue("errors during stream list extraction", extractor.getStreams().getErrors().isEmpty());
    }

    @Test
    public void testHasMoreStreams() throws Exception {
        // Setup the streams
        extractor.getStreams();
        assertTrue("don't have more streams", extractor.hasNextPage());
    }

    @Test
    public void testGetSubscriberCount() throws Exception {
        assertTrue("wrong subscriber count", extractor.getSubscriberCount() >= 1000000);
    }

    @Test
    public void testGetNextPageUrl() throws Exception {
        assertTrue(extractor.hasNextPage());
    }

    @Test
    public void testGetPage() throws Exception {
        // Setup the streams
        extractor.getStreams();
        ListExtractor.InfoItemPage nextItemsResult = extractor.getPage(extractor.getNextPageUrl());
        assertTrue("extractor didn't have next streams", !nextItemsResult.infoItemList.isEmpty());
        assertTrue("errors occurred during extraction of the next streams", nextItemsResult.errors.isEmpty());
        assertTrue("extractor didn't have more streams after getInfoItemPage", extractor.hasNextPage());
    }

}
