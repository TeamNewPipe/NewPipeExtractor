package org.schabi.newpipe.extractor.services.youtube.invidious;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.InvalidInstanceException;

import static org.junit.Assert.*;

public class InvidiousInstanceTest {

    @BeforeClass
    public static void setUp() {
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void testInvidious_snopyta_IsValid() {
        InvidiousInstance invidious = new InvidiousInstance("https://invidious.snopyta.org");
        assertTrue(invidious.isValid());
    }

    @Test
    public void testInvidious_snopyta_GetName() throws InvalidInstanceException {
        InvidiousInstance invidious = new InvidiousInstance("https://invidious.snopyta.org");
        invidious.fetchInstanceMetaData();
        assertEquals("invidious", invidious.getName());
    }

    @Test
    public void testYoutube_comIsValid() {
        InvidiousInstance youtube = new InvidiousInstance("https://youtube.com");
        assertFalse(youtube.isValid());
    }
}
