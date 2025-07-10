package org.schabi.newpipe.extractor.services.media_ccc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCStreamExtractor;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.StreamExtractor;

/**
 * Test {@link MediaCCCStreamExtractor}
 */
public class MediaCCCOggTest extends DefaultSimpleExtractorTest<StreamExtractor> {

    @Override
    protected StreamExtractor createExtractor() throws Exception {
        return MediaCCC.getStreamExtractor("https://media.ccc.de/public/events/1317");
    }

    @Test
    public void getAudioStreamsCount() throws Exception {
        assertEquals(1, extractor().getAudioStreams().size());
    }

    @Test
    public void getAudioStreamsContainOgg() throws Exception {
        for (final AudioStream stream : extractor().getAudioStreams()) {
            assertEquals("OGG", stream.getFormat().toString());
        }
    }
}
