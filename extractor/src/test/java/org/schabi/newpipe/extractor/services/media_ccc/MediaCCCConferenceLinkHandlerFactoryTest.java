package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MediaCCCConferenceLinkHandlerFactoryTest {
    private static MediaCCCConferenceLinkHandlerFactory linkHandler;

    @BeforeAll
    public static void setUp() {
        linkHandler = new MediaCCCConferenceLinkHandlerFactory();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void getId() throws ParsingException {
        assertEquals("jh20",
                linkHandler.fromUrl("https://media.ccc.de/c/jh20#278").getId());
        assertEquals("jh20",
                linkHandler.fromUrl("https://media.ccc.de/b/jh20?a=b").getId());
        assertEquals("jh20",
                linkHandler.fromUrl("https://api.media.ccc.de/public/conferences/jh20&a=b&b=c").getId());
    }

    @Test
    public void getUrl() throws ParsingException {
        assertEquals("https://media.ccc.de/c/jh20",
                linkHandler.fromUrl("https://media.ccc.de/c/jh20#278").getUrl());
        assertEquals("https://media.ccc.de/c/jh20",
                linkHandler.fromUrl("https://media.ccc.de/b/jh20?a=b").getUrl());
        assertEquals("https://media.ccc.de/c/jh20",
                linkHandler.fromUrl("https://api.media.ccc.de/public/conferences/jh20&a=b&b=c").getUrl());
        assertEquals("https://media.ccc.de/c/jh20",
                linkHandler.fromId("jh20").getUrl());
    }
}
