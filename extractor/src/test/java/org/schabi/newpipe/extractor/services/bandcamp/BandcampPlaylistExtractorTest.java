package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.bandcamp;

public class BandcampPlaylistExtractorTest {

    @BeforeClass
    public static void setUp() {
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void testCount() throws ExtractionException, IOException {
        PlaylistExtractor extractor = bandcamp.getPlaylistExtractor("https://macbenson.bandcamp.com/album/coming-of-age");
        extractor.fetchPage();

        assertEquals(5, extractor.getStreamCount());
    }
}
