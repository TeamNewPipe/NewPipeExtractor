package org.schabi.newpipe.extractor.services.media_ccc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCStreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseAudioStream;
import org.schabi.newpipe.extractor.stream.mediaformat.AudioMediaFormat;

import java.util.List;

/**
 * Test for {@link MediaCCCStreamExtractor}.
 */
public class MediaCCCOggTest extends DefaultSimpleExtractorTest<StreamExtractor> {

    @Override
    protected StreamExtractor createExtractor() throws Exception {
        return MediaCCC.getStreamExtractor("https://media.ccc.de/public/events/1317");
    }

    @Test
    public void testAudioStreams() throws Exception {
        final List<BaseAudioStream> audioStreams = extractor().getStreams()
                .stream()
                .filter(BaseAudioStream.class::isInstance)
                .map(BaseAudioStream.class::cast)
                        .toList();
        assertEquals(1, audioStreams.size());
        assertEquals(AudioMediaFormat.OGG, audioStreams.get(0).getMediaFormat());
    }
}
