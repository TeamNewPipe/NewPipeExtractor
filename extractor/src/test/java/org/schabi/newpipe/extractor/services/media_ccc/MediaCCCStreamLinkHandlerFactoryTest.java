package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCStreamLinkHandlerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MediaCCCStreamLinkHandlerFactoryTest {
    private static MediaCCCStreamLinkHandlerFactory linkHandler;

    @BeforeAll
    public static void setUp() {
        linkHandler = new MediaCCCStreamLinkHandlerFactory();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void getId() throws ParsingException {
        assertEquals("jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler.fromUrl("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020").getId());
        assertEquals("jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler.fromUrl("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020?a=b").getId());
        assertEquals("jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler.fromUrl("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020#3").getId());
        assertEquals("jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler.fromUrl("https://api.media.ccc.de/public/events/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020&a=b").getId());
    }

    @Test
    public void getUrl() throws ParsingException {
        assertEquals("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler.fromUrl("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020").getUrl());
        assertEquals("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler.fromUrl("https://api.media.ccc.de/public/events/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020?b=a&a=b").getUrl());
        assertEquals("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler.fromId("jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020").getUrl());
    }
}
