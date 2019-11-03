package org.schabi.newpipe.extractor.services.youtube.stream;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Localization;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

/**
 * Test for {@link YoutubeStreamLinkHandlerFactory}
 */
public class YoutubeStreamExtractorAgeRestrictedTest extends YoutubeStreamExtractorBaseTest {

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance(), new Localization("GB", "en"));
        extractor = (YoutubeStreamExtractor) YouTube
                .getStreamExtractor("https://www.youtube.com/watch?v=MmBeUZqv1QA");
        extractor.fetchPage();
    }

    @Test
    public void testGetAgeLimit() throws ParsingException {
        assertEquals(18, extractor.getAgeLimit());
    }

    @Test
    public void testGetLength() throws ParsingException {
        assertEquals(1789, extractor.getLength());
    }

    @Test
    public void testGetAudioStreams() throws IOException, ExtractionException {
        super.testGetAudioStreams();
        // audio streams are not always necessary
        assertFalse(extractor.getAudioStreams().isEmpty());
    }

    @Test
    @Ignore("Apparently age restricted videos have no related videos")
    @Override
    public void testGetRelatedVideos() throws IOException, ExtractionException {
        super.testGetRelatedVideos();
    }

    @Test
    public void testGetSubtitlesListDefault() throws IOException, ExtractionException {
        // Video (/view?v=MmBeUZqv1QA) set in the setUp() method has no captions => null
        assertTrue(extractor.getSubtitlesDefault().isEmpty());
    }

    @Test
    public void testGetSubtitlesList() throws IOException, ExtractionException {
        // Video (/view?v=MmBeUZqv1QA) set in the setUp() method has no captions => null
        assertTrue(extractor.getSubtitles(MediaFormat.TTML).isEmpty());
    }
}
