package org.schabi.newpipe.extractor.services.peertube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;

/**
 * Test for {@link PeertubeChannelLinkHandlerFactory}
 */
public class PeertubeChannelLinkHandlerFactoryTest {

    private static PeertubeChannelLinkHandlerFactory linkHandler;

    @BeforeClass
    public static void setUp() {
        PeerTube.setInstance(new PeertubeInstance("https://peertube.mastodon.host", "PeerTube on Mastodon.host"));
        linkHandler = PeertubeChannelLinkHandlerFactory.getInstance();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void acceptUrlTest() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://peertube.mastodon.host/accounts/kranti@videos.squat.net"));
        assertTrue(linkHandler.acceptUrl("https://peertube.mastodon.host/video-channels/7682d9f2-07be-4622-862e-93ec812e2ffa"));
    }

    @Test
    public void getIdFromUrl() throws ParsingException {
        assertEquals("accounts/kranti@videos.squat.net", linkHandler.fromUrl("https://peertube.mastodon.host/accounts/kranti@videos.squat.net").getId());
        assertEquals("accounts/kranti@videos.squat.net", linkHandler.fromUrl("https://peertube.mastodon.host/accounts/kranti@videos.squat.net/videos").getId());
        assertEquals("video-channels/7682d9f2-07be-4622-862e-93ec812e2ffa", linkHandler.fromUrl("https://peertube.mastodon.host/video-channels/7682d9f2-07be-4622-862e-93ec812e2ffa/videos").getId());
    }

    @Test
    public void getUrlFromId() throws ParsingException {
        assertEquals("https://peertube.mastodon.host/api/v1/video-channels/7682d9f2-07be-4622-862e-93ec812e2ffa", linkHandler.fromId("video-channels/7682d9f2-07be-4622-862e-93ec812e2ffa").getUrl());
        assertEquals("https://peertube.mastodon.host/api/v1/accounts/kranti@videos.squat.net", linkHandler.fromId("accounts/kranti@videos.squat.net").getUrl());
        assertEquals("https://peertube.mastodon.host/api/v1/accounts/kranti@videos.squat.net", linkHandler.fromId("kranti@videos.squat.net").getUrl());
    }
}
