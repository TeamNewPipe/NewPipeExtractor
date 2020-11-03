package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeCommentsLinkHandlerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class YouTubeCommentsLinkHandlerFactoryTest {

    private static YoutubeCommentsLinkHandlerFactory linkHandler;

    @BeforeClass
    public static void setUp() {
        NewPipe.init(DownloaderTestImpl.getInstance());
        linkHandler = YoutubeCommentsLinkHandlerFactory.getInstance();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdWithNullAsUrl() throws ParsingException {
        linkHandler.fromId(null);
    }

    @Test
    public void getIdFromYt() throws ParsingException {
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://www.youtube.com/watch?v=VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://m.youtube.com/watch?v=VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://youtube.com/watch?v=VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://WWW.youtube.com/watch?v=VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://youtu.be/VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://youtu.be/VM_6n762j6M&t=20").getId());
    }

    @Test
    public void testAcceptUrl() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://www.youtube.com/watch?v=VM_6n762j6M&t=20"));
        assertTrue(linkHandler.acceptUrl("https://WWW.youtube.com/watch?v=VM_6n762j6M&t=20"));
        assertTrue(linkHandler.acceptUrl("https://youtube.com/watch?v=VM_6n762j6M&t=20"));
        assertTrue(linkHandler.acceptUrl("https://youtu.be/VM_6n762j6M&t=20"));
    }

    @Test
    public void testDeniesUrl() throws ParsingException {
        assertFalse(linkHandler.acceptUrl("https://www.you com/watch?v=VM_6n762j6M"));
        assertFalse(linkHandler.acceptUrl("https://com/watch?v=VM_6n762j6M"));
        assertFalse(linkHandler.acceptUrl("htt ://com/watch?v=VM_6n762j6M"));
        assertFalse(linkHandler.acceptUrl("ftp://www.youtube.com/watch?v=VM_6n762j6M"));
    }

    @Test
    public void getIdFromInvidious() throws ParsingException {
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://www.invidio.us/watch?v=VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://invidio.us/watch?v=VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://INVIDIO.US/watch?v=VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://invidio.us/VM_6n762j6M").getId());
        assertEquals("VM_6n762j6M", linkHandler.fromUrl("https://invidio.us/VM_6n762j6M&t=20").getId());
    }

}
