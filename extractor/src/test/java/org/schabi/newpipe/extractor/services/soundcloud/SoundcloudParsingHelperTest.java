package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

public class SoundcloudParsingHelperTest {
    @BeforeAll
    public static void setUp() {
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void assertThatHardcodedClientIdIsValid() throws Exception {
        assertTrue(SoundcloudParsingHelper.checkIfHardcodedClientIdIsValid(),
                "Hardcoded client id is not valid anymore");
    }

    @Test
    public void assertHardCodedClientIdMatchesCurrentClientId() throws IOException, ExtractionException {
        assertEquals(
                SoundcloudParsingHelper.HARDCODED_CLIENT_ID,
                SoundcloudParsingHelper.clientId(),
                "Hardcoded client doesn't match extracted clientId");
    }

    @Test
    public void resolveUrlWithEmbedPlayerTest() throws Exception {
        assertEquals("https://soundcloud.com/trapcity", SoundcloudParsingHelper.resolveUrlWithEmbedPlayer("https://api.soundcloud.com/users/26057743"));
        assertEquals("https://soundcloud.com/nocopyrightsounds", SoundcloudParsingHelper.resolveUrlWithEmbedPlayer("https://api.soundcloud.com/users/16069159"));
        assertEquals("https://soundcloud.com/trapcity", SoundcloudParsingHelper.resolveUrlWithEmbedPlayer("https://api-v2.soundcloud.com/users/26057743"));
        assertEquals("https://soundcloud.com/nocopyrightsounds", SoundcloudParsingHelper.resolveUrlWithEmbedPlayer("https://api-v2.soundcloud.com/users/16069159"));
    }

    @Test
    public void resolveIdWithWidgetApiTest() throws Exception {
        assertEquals("26057743", SoundcloudParsingHelper.resolveIdWithWidgetApi("https://soundcloud.com/trapcity"));
        assertEquals("16069159", SoundcloudParsingHelper.resolveIdWithWidgetApi("https://soundcloud.com/nocopyrightsounds"));
    }

}