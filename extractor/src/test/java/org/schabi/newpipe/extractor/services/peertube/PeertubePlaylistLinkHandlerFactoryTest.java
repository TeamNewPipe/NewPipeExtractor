package org.schabi.newpipe.extractor.services.peertube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubePlaylistLinkHandlerFactory;

/**
 * Test for {@link PeertubePlaylistLinkHandlerFactory}
 */
public class PeertubePlaylistLinkHandlerFactoryTest {

    private static PeertubePlaylistLinkHandlerFactory linkHandler;

    @BeforeClass
    public static void setUp() {
        linkHandler = PeertubePlaylistLinkHandlerFactory.getInstance();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void acceptUrlTest() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://peertube.mastodon.host/video-channels/b45e84fb-c47f-475b-94f2-718126154d33/videos"));
    }

    @Test
    public void getIdFromUrl() throws ParsingException {
        assertEquals("b45e84fb-c47f-475b-94f2-718126154d33", linkHandler.fromUrl("https://peertube.mastodon.host/video-channels/b45e84fb-c47f-475b-94f2-718126154d33").getId());
        assertEquals("b45e84fb-c47f-475b-94f2-718126154d33", linkHandler.fromUrl("https://peertube.mastodon.host/video-channels/b45e84fb-c47f-475b-94f2-718126154d33/videos").getId());
    }
}
