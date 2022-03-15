package org.schabi.newpipe.extractor.services.soundcloud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;

class SoundcloudParsingHelperTest {
    @BeforeAll
    public static void setUp() {
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    void resolveUrlWithEmbedPlayerTest() throws Exception {
        assertEquals("https://soundcloud.com/trapcity", SoundcloudParsingHelper.resolveUrlWithEmbedPlayer("https://api.soundcloud.com/users/26057743"));
        assertEquals("https://soundcloud.com/nocopyrightsounds", SoundcloudParsingHelper.resolveUrlWithEmbedPlayer("https://api.soundcloud.com/users/16069159"));
        assertEquals("https://soundcloud.com/trapcity", SoundcloudParsingHelper.resolveUrlWithEmbedPlayer("https://api-v2.soundcloud.com/users/26057743"));
        assertEquals("https://soundcloud.com/nocopyrightsounds", SoundcloudParsingHelper.resolveUrlWithEmbedPlayer("https://api-v2.soundcloud.com/users/16069159"));
    }

    @Test
    void resolveIdWithWidgetApiTest() throws Exception {
        assertEquals("26057743", SoundcloudParsingHelper.resolveIdWithWidgetApi("https://soundcloud.com/trapcity"));
        assertEquals("16069159", SoundcloudParsingHelper.resolveIdWithWidgetApi("https://soundcloud.com/nocopyrightsounds"));
    }

}