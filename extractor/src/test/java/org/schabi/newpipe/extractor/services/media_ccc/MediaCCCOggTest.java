package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCStreamExtractor;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.utils.Localization;


import static junit.framework.TestCase.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

/**
 * Test {@link MediaCCCStreamExtractor}
 */
public class MediaCCCOggTest {
    // test against https://api.media.ccc.de/public/events/1317
    private static StreamExtractor extractor;

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(Downloader.getInstance(), new Localization("GB", "en"));

        extractor =  MediaCCC.getStreamExtractor("https://api.media.ccc.de/public/events/1317");
        extractor.fetchPage();
    }

    @Test
    public void getAudioStreamsCount() throws Exception {
        assertEquals(1, extractor.getAudioStreams().size());
    }

    @Test
    public void getAudioStreamsContainOgg() throws Exception {
        for(AudioStream stream : extractor.getAudioStreams()) {
            System.out.println(stream.getFormat());
        }
    }
}
