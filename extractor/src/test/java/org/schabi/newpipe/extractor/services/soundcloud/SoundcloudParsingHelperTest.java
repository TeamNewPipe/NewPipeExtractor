package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.*;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;

import static org.junit.Assert.*;

public class SoundcloudParsingHelperTest {
    @BeforeClass
    public static void setUp() {
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void assertThatHardcodedClientIdIsValid() throws Exception {
        assertTrue("Hardcoded client id is not valid anymore",
                SoundcloudParsingHelper.checkIfHardcodedClientIdIsValid(DownloaderTestImpl.getInstance()));
    }

    @Test
    public void resolveUrlWithEmbedPlayerTest() throws Exception {
        Assert.assertEquals("https://soundcloud.com/trapcity", SoundcloudParsingHelper.resolveUrlWithEmbedPlayer("https://api.soundcloud.com/users/26057743"));
        Assert.assertEquals("https://soundcloud.com/nocopyrightsounds", SoundcloudParsingHelper.resolveUrlWithEmbedPlayer("https://api.soundcloud.com/users/16069159"));
    }

    @Test
    public void resolveIdWithEmbedPlayerTest() throws Exception {
        Assert.assertEquals("26057743", SoundcloudParsingHelper.resolveIdWithEmbedPlayer("https://soundcloud.com/trapcity"));
        Assert.assertEquals("16069159", SoundcloudParsingHelper.resolveIdWithEmbedPlayer("https://soundcloud.com/nocopyrightsounds"));

    }

}