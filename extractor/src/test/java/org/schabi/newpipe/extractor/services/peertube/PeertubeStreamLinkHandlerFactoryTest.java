package org.schabi.newpipe.extractor.services.peertube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeStreamLinkHandlerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;

/**
 * Test for {@link PeertubeStreamLinkHandlerFactory}
 */
public class PeertubeStreamLinkHandlerFactoryTest {

    private static PeertubeStreamLinkHandlerFactory linkHandler;

    @BeforeAll
    public static void setUp() {
        PeerTube.setInstance(new PeertubeInstance("https://framatube.org", "Framatube"));
        linkHandler = PeertubeStreamLinkHandlerFactory.getInstance();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void getId() throws Exception {
        assertEquals("986aac60-1263-4f73-9ce5-36b18225cb60",
                linkHandler.fromUrl("https://peertube.mastodon.host/videos/watch/986aac60-1263-4f73-9ce5-36b18225cb60").getId());
        assertEquals("986aac60-1263-4f73-9ce5-36b18225cb60",
                linkHandler.fromUrl("https://peertube.mastodon.host/videos/watch/986aac60-1263-4f73-9ce5-36b18225cb60?fsdafs=fsafa").getId());
        assertEquals("986aac60-1263-4f73-9ce5-36b18225cb60",
                linkHandler.fromUrl("https://peertube.mastodon.host/api/v1/videos/watch/986aac60-1263-4f73-9ce5-36b18225cb60").getId());
        assertEquals("986aac60-1263-4f73-9ce5-36b18225cb60",
                linkHandler.fromUrl("https://peertube.mastodon.host/api/v1/videos/watch/986aac60-1263-4f73-9ce5-36b18225cb60?fsdafs=fsafa").getId());

        assertEquals("9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler.fromUrl("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d").getId());
        assertEquals("9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler.fromUrl("https://framatube.org/videos/embed/9c9de5e8-0a1e-484a-b099-e80766180a6d").getId());
        assertEquals("9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler.fromUrl("https://framatube.org/w/9c9de5e8-0a1e-484a-b099-e80766180a6d").getId());
    }

    @Test
    public void getUrl() throws Exception {
        assertEquals("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler.fromId("9c9de5e8-0a1e-484a-b099-e80766180a6d").getUrl());
        assertEquals("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler.fromUrl("https://framatube.org/api/v1/videos/9c9de5e8-0a1e-484a-b099-e80766180a6d").getUrl());
        assertEquals("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler.fromUrl("https://framatube.org/videos/embed/9c9de5e8-0a1e-484a-b099-e80766180a6d").getUrl());
        assertEquals("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler.fromUrl("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d").getUrl());
        assertEquals("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler.fromUrl("https://framatube.org/w/9c9de5e8-0a1e-484a-b099-e80766180a6d").getUrl());
    }


    @Test
    public void testAcceptUrl() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://peertube.mastodon.host/videos/watch/986aac60-1263-4f73-9ce5-36b18225cb60"));
        assertTrue(linkHandler.acceptUrl("https://peertube.mastodon.host/videos/watch/986aac60-1263-4f73-9ce5-36b18225cb60?fsdafs=fsafa"));

        assertTrue(linkHandler.acceptUrl("https://framatube.org/api/v1/videos/9c9de5e8-0a1e-484a-b099-e80766180a6d"));
        assertTrue(linkHandler.acceptUrl("https://framatube.org/videos/embed/9c9de5e8-0a1e-484a-b099-e80766180a6d"));
        assertTrue(linkHandler.acceptUrl("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d"));
        assertTrue(linkHandler.acceptUrl("https://framatube.org/w/9c9de5e8-0a1e-484a-b099-e80766180a6d"));

        // make sure playlists aren't accepted
        assertFalse(linkHandler.acceptUrl("https://framatube.org/w/p/dacdc4ef-5160-4846-9b70-a655880da667"));
        assertFalse(linkHandler.acceptUrl("https://framatube.org/videos/watch/playlist/dacdc4ef-5160-4846-9b70-a655880da667"));
    }
}
