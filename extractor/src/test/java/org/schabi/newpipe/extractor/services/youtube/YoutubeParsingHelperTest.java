package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class YoutubeParsingHelperTest {
    @BeforeClass
    public static void setUp() {
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void testIsHardcodedClientVersionValid() throws IOException, ExtractionException {
        assertTrue("Hardcoded client version is not valid anymore",
                YoutubeParsingHelper.isHardcodedClientVersionValid());
    }
}
