package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.IOException;

class YoutubeJavaScriptExtractorTest {
    private static final String RESOURCE_PATH =
            DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/js_extractor/";

    @BeforeEach
    public void setup() {
        YoutubeTestsUtils.ensureStateless();
    }

    @Test
    void testExtractJavaScriptUrlIframe() throws ParsingException {
        NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "urlWithIframeResource"));

        assertTrue(YoutubeJavaScriptExtractor.extractJavaScriptUrlWithIframeResource()
                .endsWith("base.js"));
    }

    @Test
    void testExtractJavaScriptUrlEmbed() throws ParsingException {
        NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "embedWatchPage"));

        assertTrue(YoutubeJavaScriptExtractor.extractJavaScriptUrlWithEmbedWatchPage("d4IGg5dqeO8")
                .endsWith("base.js"));
    }

    @Test
    void testExtractJavaScript__success() throws ParsingException {
        NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "playerCode"));

        final String playerJsCode = YoutubeJavaScriptExtractor.extractJavaScriptPlayerCode("d4IGg5dqeO8");
        assertPlayerJsCode(playerJsCode);
    }

    @Test
    void testExtractJavaScript__invalidVideoId__success() throws ParsingException {
        NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "playerCodeInvalidVideoId"));

        String playerJsCode = YoutubeJavaScriptExtractor.extractJavaScriptPlayerCode("not_a_video_id");
        assertPlayerJsCode(playerJsCode);

        playerJsCode = YoutubeJavaScriptExtractor.extractJavaScriptPlayerCode("11-chars123");
        assertPlayerJsCode(playerJsCode);

    }

    private void assertPlayerJsCode(final String playerJsCode) {
        ExtractorAsserts.assertContains(" Copyright The Closure Library Authors.\n"
                + " SPDX-License-Identifier: Apache-2.0", playerJsCode);
        ExtractorAsserts.assertContains("var _yt_player", playerJsCode);
    }
}