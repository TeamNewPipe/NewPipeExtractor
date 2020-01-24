package org.schabi.newpipe.extractor.services.peertube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory;

/**
 * Test for {@link PeertubeChannelLinkHandlerFactory}
 */
public class PeertubeChannelLinkHandlerFactoryTest {

    private static PeertubeChannelLinkHandlerFactory linkHandler;

    @BeforeClass
    public static void setUp() {
        linkHandler = PeertubeChannelLinkHandlerFactory.getInstance();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void acceptUrlTest() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://peertube.mastodon.host/accounts/kranti@videos.squat.net"));
    }

    @Test
    public void getIdFromUrl() throws ParsingException {
        assertEquals("kranti@videos.squat.net", linkHandler.fromUrl("https://peertube.mastodon.host/accounts/kranti@videos.squat.net").getId());
        assertEquals("kranti@videos.squat.net", linkHandler.fromUrl("https://peertube.mastodon.host/accounts/kranti@videos.squat.net/videos").getId());
    }
}
