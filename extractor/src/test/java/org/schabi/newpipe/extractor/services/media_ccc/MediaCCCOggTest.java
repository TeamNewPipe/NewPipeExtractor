package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCStreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.streamdata.stream.AudioStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

/**
 * Test {@link MediaCCCStreamExtractor}
 */
public class MediaCCCOggTest {
    // test against https://media.ccc.de/public/events/1317
    private static StreamExtractor extractor;

    @BeforeAll
    public static void setUpClass() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());

        extractor = MediaCCC.getStreamExtractor("https://media.ccc.de/public/events/1317");
        extractor.fetchPage();
    }

    @Test
    public void getAudioStreamsCount() throws Exception {
        assertEquals(1, extractor.getAudioStreams().size());
    }

    @Test
    public void getAudioStreamsContainOgg() throws Exception {
        for (AudioStream stream : extractor.getAudioStreams()) {
            assertEquals("ogg", stream.mediaFormat().name());
        }
    }
}
