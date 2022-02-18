package org.schabi.newpipe.extractor.services.peertube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeCommentsLinkHandlerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link PeertubeCommentsLinkHandlerFactory}
 */
public class PeertubeCommentsLinkHandlerFactoryTest {

    private static PeertubeCommentsLinkHandlerFactory linkHandler;

    @BeforeAll
    public static void setUp() {
        linkHandler = PeertubeCommentsLinkHandlerFactory.getInstance();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void acceptUrlTest() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://framatube.org/videos/watch/kkGMgK9ZtnKfYAgnEtQxbv"));
        assertTrue(linkHandler.acceptUrl("https://framatube.org/w/kkGMgK9ZtnKfYAgnEtQxbv"));
        assertTrue(linkHandler.acceptUrl("https://framatube.org/api/v1/videos/kkGMgK9ZtnKfYAgnEtQxbv/comment-threads?start=0&count=10&sort=-createdAt"));
        assertTrue(linkHandler.acceptUrl("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d"));
        assertTrue(linkHandler.acceptUrl("https://framatube.org/w/9c9de5e8-0a1e-484a-b099-e80766180a6d"));
        assertTrue(linkHandler.acceptUrl("https://framatube.org/api/v1/videos/9c9de5e8-0a1e-484a-b099-e80766180a6d/comment-threads?start=0&count=10&sort=-createdAt"));
    }

    @Test
    public void getIdFromUrl() throws ParsingException {
        assertEquals("kkGMgK9ZtnKfYAgnEtQxbv",
                linkHandler.fromUrl("https://framatube.org/w/kkGMgK9ZtnKfYAgnEtQxbv").getId());
        assertEquals("kkGMgK9ZtnKfYAgnEtQxbv",
                linkHandler.fromUrl("https://framatube.org/videos/watch/kkGMgK9ZtnKfYAgnEtQxbv").getId());
        assertEquals("kkGMgK9ZtnKfYAgnEtQxbv",
                linkHandler.fromUrl("https://framatube.org/api/v1/videos/kkGMgK9ZtnKfYAgnEtQxbv/comment-threads").getId());
        assertEquals("kkGMgK9ZtnKfYAgnEtQxbv",
                linkHandler.fromUrl("https://framatube.org/api/v1/videos/kkGMgK9ZtnKfYAgnEtQxbv/comment-threads?start=0&count=10&sort=-createdAt").getId());

        assertEquals("9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler.fromUrl("https://framatube.org/w/9c9de5e8-0a1e-484a-b099-e80766180a6d").getId());
        assertEquals("9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler.fromUrl("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d").getId());
        assertEquals("9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler.fromUrl("https://framatube.org/api/v1/videos/9c9de5e8-0a1e-484a-b099-e80766180a6d/comment-threads").getId());
        assertEquals("9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler.fromUrl("https://framatube.org/api/v1/videos/9c9de5e8-0a1e-484a-b099-e80766180a6d/comment-threads?start=0&count=10&sort=-createdAt").getId());
    }
}
