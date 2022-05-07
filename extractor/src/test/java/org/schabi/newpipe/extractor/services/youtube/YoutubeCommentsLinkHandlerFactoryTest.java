package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.youtube.linkHandler.YoutubeCommentsLinkHandlerFactory;

public class YoutubeCommentsLinkHandlerFactoryTest {

    private static YoutubeCommentsLinkHandlerFactory linkHandler;

    @BeforeAll
    public static void setUp() {
        NewPipe.init(DownloaderTestImpl.getInstance());
        linkHandler = YoutubeCommentsLinkHandlerFactory.getInstance();
    }

    @Test
    void getIdWithNullAsUrl() {
        assertThrows(IllegalArgumentException.class, () -> linkHandler.fromId(null));
    }

    @Test
    void getIdFromYt() throws ParsingException {
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://www.youtube.com/watch?v=VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://m.youtube.com/watch?v=VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://youtube.com/watch?v=VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://WWW.youtube.com/watch?v=VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://youtu.be/VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://youtu.be/VM_6n762j6M&t=20").getId());
    }

    @Test
    void testAcceptUrl() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/watch?v=VM_6n762j6M&t=20"));
        assertTrue(linkHandler.acceptUrl("https://WWW.youtube.com/watch?v=VM_6n762j6M&t=20"));
        assertTrue(linkHandler.acceptUrl("https://youtube.com/watch?v=VM_6n762j6M&t=20"));
        assertTrue(linkHandler.acceptUrl("https://youtu.be/VM_6n762j6M&t=20"));
    }

    @Test
    void testDeniesUrl() throws ParsingException {
        assertFalse(linkHandler.acceptUrl("https://www.you com/watch?v=VM_6n762j6M"));
        assertFalse(linkHandler.acceptUrl("https://com/watch?v=VM_6n762j6M"));
        assertFalse(linkHandler.acceptUrl("htt ://com/watch?v=VM_6n762j6M"));
        assertFalse(linkHandler.acceptUrl("ftp://www.youtube.com/watch?v=VM_6n762j6M"));
    }

    @Test
    void getIdFromInvidious() throws ParsingException {
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://www.invidious.fdn.fr/watch?v=VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://invidious.fdn.fr/watch?v=VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://INVIDIOUS.FDN.FR/watch?v=VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://invidious.fdn.fr/VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://invidious.fdn.fr/VM_6n762j6M&t=20").getId());
    }

    @Test
    void getIdFromY2ube() throws ParsingException {
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://y2u.be/VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://Y2U.Be/VM_6n762j6M").getId());
    }

}
