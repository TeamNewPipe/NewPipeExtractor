package org.schabi.newpipe.extractor.services.peertube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubePlaylistLinkHandlerFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link PeertubePlaylistLinkHandlerFactory}
 */
public class PeertubePlaylistLinkHandlerFactoryTest {

    private static PeertubePlaylistLinkHandlerFactory linkHandler;

    @BeforeAll
    public static void setUp() {
        linkHandler = PeertubePlaylistLinkHandlerFactory.getInstance();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    void acceptUrlTest() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://framatube.org/videos/watch/playlist/d8ca79f9-e4c7-4269-8183-d78ed269c909"));
        assertTrue(linkHandler.acceptUrl("https://framatube.org/w/p/d8ca79f9-e4c7-4269-8183-d78ed269c909"));
        assertTrue(linkHandler.acceptUrl("https://framatube.org/videos/watch/playlist/d8ca79f9-e4c7-4269-8183-d78ed269c909/videos"));
        assertTrue(linkHandler.acceptUrl("https://framatube.org/videos/watch/playlist/dacdc4ef-5160-4846-9b70-a655880da667"));
        assertTrue(linkHandler.acceptUrl("https://framatube.org/w/p/dacdc4ef-5160-4846-9b70-a655880da667"));
        assertTrue(linkHandler.acceptUrl("https://framatube.org/videos/watch/playlist/96b0ee2b-a5a7-4794-8769-58d8ccb79ab7"));
        assertTrue(linkHandler.acceptUrl("https://framatube.org/w/p/96b0ee2b-a5a7-4794-8769-58d8ccb79ab7"));
    }

    @Test
    void getIdFromUrl() throws ParsingException {
        assertEquals("d8ca79f9-e4c7-4269-8183-d78ed269c909", linkHandler.getId("https://framatube.org/videos/watch/playlist/d8ca79f9-e4c7-4269-8183-d78ed269c909"));
        assertEquals("d8ca79f9-e4c7-4269-8183-d78ed269c909", linkHandler.getId("https://framatube.org/w/p/d8ca79f9-e4c7-4269-8183-d78ed269c909"));
        assertEquals("dacdc4ef-5160-4846-9b70-a655880da667", linkHandler.getId("https://framatube.org/videos/watch/playlist/dacdc4ef-5160-4846-9b70-a655880da667"));
        assertEquals("dacdc4ef-5160-4846-9b70-a655880da667", linkHandler.getId("https://framatube.org/w/p/dacdc4ef-5160-4846-9b70-a655880da667"));
        assertEquals("bfc145f5-1be7-48a6-9b9e-4f1967199dad", linkHandler.getId("https://framatube.org/videos/watch/playlist/bfc145f5-1be7-48a6-9b9e-4f1967199dad"));
        assertEquals("bfc145f5-1be7-48a6-9b9e-4f1967199dad", linkHandler.getId("https://framatube.org/w/p/bfc145f5-1be7-48a6-9b9e-4f1967199dad"));
        assertEquals("96b0ee2b-a5a7-4794-8769-58d8ccb79ab7", linkHandler.getId("https://framatube.org/videos/watch/playlist/96b0ee2b-a5a7-4794-8769-58d8ccb79ab7"));
        assertEquals("96b0ee2b-a5a7-4794-8769-58d8ccb79ab7", linkHandler.getId("https://framatube.org/w/p/96b0ee2b-a5a7-4794-8769-58d8ccb79ab7"));
    }

    @Test
    void getUrl() {
        assertDoesNotThrow(() -> linkHandler.fromUrl("https://framatube.org/videos/watch/playlist/d8ca79f9-e4c7-4269-8183-d78ed269c909").getUrl());
        assertDoesNotThrow(() -> linkHandler.fromUrl("https://framatube.org/w/p/d8ca79f9-e4c7-4269-8183-d78ed269c909").getUrl());
        assertDoesNotThrow(() -> linkHandler.fromUrl("https://framatube.org/w/p/sLFbqXsw7sPR3AfvqQSBZB").getUrl());
    }
}
