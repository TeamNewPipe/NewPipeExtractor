package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.IOException;

public class YoutubeJavaScriptExtractorTest {

    @BeforeEach
    public void setup() throws IOException {
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void testExtractJavaScriptUrlIframe() throws ParsingException {
        assertTrue(YoutubeJavaScriptExtractor.extractJavaScriptUrl().endsWith("base.js"));
    }

    @Test
    public void testExtractJavaScriptUrlEmbed() throws ParsingException {
        assertTrue(YoutubeJavaScriptExtractor.extractJavaScriptUrl("d4IGg5dqeO8").endsWith("base.js"));
    }

    @Test
    public void testExtractJavaScript__success() throws ParsingException {
        String playerJsCode = YoutubeJavaScriptExtractor.extractJavaScriptCode("d4IGg5dqeO8");
        assertPlayerJsCode(playerJsCode);

        playerJsCode = YoutubeJavaScriptExtractor.extractJavaScriptCode();
        assertPlayerJsCode(playerJsCode);
    }

    @Test
    public void testExtractJavaScript__invalidVideoId__success() throws ParsingException {
        String playerJsCode = YoutubeJavaScriptExtractor.extractJavaScriptCode("not_a_video_id");
        assertPlayerJsCode(playerJsCode);

        playerJsCode = YoutubeJavaScriptExtractor.extractJavaScriptCode("11-chars123");
        assertPlayerJsCode(playerJsCode);

    }

    private void assertPlayerJsCode(final String playerJsCode) {
        ExtractorAsserts.assertContains(" Copyright The Closure Library Authors.\n"
                + " SPDX-License-Identifier: Apache-2.0", playerJsCode);
        ExtractorAsserts.assertContains("var _yt_player", playerJsCode);
    }
}