package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.InitNewPipeTest;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

class YoutubeJavaScriptExtractorTest {

    void initNewPipe(final String useCase) {
        InitNewPipeTest.initNewPipe(this.getClass(), useCase);
        YoutubeTestsUtils.ensureStateless();
    }

    @Test
    void testExtractJavaScriptUrlIframe() throws ParsingException {
        initNewPipe("urlWithIframeResource");

        assertTrue(YoutubeJavaScriptExtractor.extractJavaScriptUrlWithIframeResource()
                .endsWith("base.js"));
    }

    @Test
    void testExtractJavaScriptUrlEmbed() throws ParsingException {
        initNewPipe("embedWatchPage");

        assertTrue(YoutubeJavaScriptExtractor.extractJavaScriptUrlWithEmbedWatchPage("d4IGg5dqeO8")
                .endsWith("base.js"));
    }

    @Test
    void testExtractJavaScript__success() throws ParsingException {
        initNewPipe("playerCode");

        final String playerJsCode = YoutubeJavaScriptExtractor.extractJavaScriptPlayerCode("d4IGg5dqeO8");
        assertPlayerJsCode(playerJsCode);
    }

    @Test
    void testExtractJavaScript__invalidVideoId__success() throws ParsingException {
        initNewPipe("playerCodeInvalidVideoId");

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