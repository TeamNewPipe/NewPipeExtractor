package org.schabi.newpipe.extractor.services.youtube;

import org.junit.Before;
import org.junit.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class YoutubeJavaScriptExtractorTest {

    @Before
    public void setup() throws IOException {
        NewPipe.init(DownloaderTestImpl.getInstance());
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
        assertThat(playerJsCode, allOf(
                containsString(" Copyright The Closure Library Authors.\n"
                        + " SPDX-License-Identifier: Apache-2.0"),
                containsString("var _yt_player")));
    }
}