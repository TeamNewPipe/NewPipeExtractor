package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.Before;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.user.UserExtractor;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

/**
 * Test for {@link UserExtractor}
 */

public class SoundcloudUserExtractorTest {

    UserExtractor extractor;

    @Before
    public void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        extractor = SoundCloud.getService()
                .getUserExtractor("https://soundcloud.com/liluzivert");
    }

    @Test
    public void testGetDownloader() throws Exception {
        assertNotNull(NewPipe.getDownloader());
    }

    @Test
    public void testGetUserName() throws Exception {
        assertEquals(extractor.getUserName(), "LIL UZI VERT");
    }

    @Test
    public void testGetDescription() throws Exception {
        assertEquals(extractor.getDescription(), "");
    }

    @Test
    public void testGetAvatarUrl() throws Exception {
        assertEquals(extractor.getAvatarUrl(), "https://a1.sndcdn.com/images/default_avatar_large.png");
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
        assertTrue("don't have more streams", extractor.hasMoreStreams());
    }

    @Test
    public void testGetSubscriberCount() throws Exception {
        assertTrue("wrong subscriber count", extractor.getSubscriberCount() >= 1224324);
    }

    @Test
    public void testGetNextStreams() throws Exception {
        // Setup the streams
        extractor.getStreams();
        assertTrue("extractor didn't have next streams", !extractor.getNextStreams().nextItemsList.isEmpty());
        assertTrue("extractor didn't have more streams after getNextStreams", extractor.hasMoreStreams());
    }

}
