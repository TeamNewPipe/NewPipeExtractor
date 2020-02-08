package org.schabi.newpipe.extractor.services.peertube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeCommentsLinkHandlerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link PeertubeCommentsLinkHandlerFactory}
 */
public class PeertubeCommentsLinkHandlerFactoryTest {

    private static PeertubeCommentsLinkHandlerFactory linkHandler;

    @BeforeClass
    public static void setUp() {
        linkHandler = PeertubeCommentsLinkHandlerFactory.getInstance();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void acceptUrlTest() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://peertube.mastodon.host/api/v1/videos/19319/comment-threads?start=0&count=10&sort=-createdAt"));
    }

    @Test
    public void getIdFromUrl() throws ParsingException {
        assertEquals("19319", linkHandler.fromUrl("https://peertube.mastodon.host/api/v1/videos/19319/comment-threads").getId());
        assertEquals("19319", linkHandler.fromUrl("https://peertube.mastodon.host/api/v1/videos/19319/comment-threads?start=0&count=10&sort=-createdAt").getId());
    }
}
