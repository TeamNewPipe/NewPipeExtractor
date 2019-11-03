package org.schabi.newpipe.extractor.services.youtube.stream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.utils.Localization;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class YoutubeStreamExtractorTimestampTest extends YoutubeStreamExtractorBaseTest {

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance(), new Localization("GB", "en"));
        extractor = (YoutubeStreamExtractor) YouTube
                .getStreamExtractor("https://youtu.be/FmG385_uUys?t=174");
        extractor.fetchPage();
    }

    @Override
    @Test
    public void testGetTimestamp() throws ParsingException {
        assertEquals(extractor.getTimeStamp(), 174);
    }
}
