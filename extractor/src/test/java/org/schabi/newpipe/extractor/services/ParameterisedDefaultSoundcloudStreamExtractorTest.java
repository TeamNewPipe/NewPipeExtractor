package org.schabi.newpipe.extractor.services;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.services.testcases.SoundcloudStreamExtractorTestCase;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public abstract class ParameterisedDefaultSoundcloudStreamExtractorTest
    extends ParameterisedDefaultStreamExtractorTest<SoundcloudStreamExtractorTestCase> {
    protected ParameterisedDefaultSoundcloudStreamExtractorTest(SoundcloudStreamExtractorTestCase testCase) {
        super(testCase);
    }

    final Pattern mp3CdnUrlPattern = Pattern.compile("-media\\.sndcdn\\.com/[a-zA-Z0-9]{12}\\.128\\.mp3");

    @Override
    @Test
    public void testAudioStreams() throws Exception {
        super.testAudioStreams();
        final List<AudioStream> audioStreams = extractor.getAudioStreams();
        assertEquals(3, audioStreams.size()); // 2 MP3 streams (1 progressive, 1 HLS) and 1 OPUS
        audioStreams.forEach(audioStream -> {
            final DeliveryMethod deliveryMethod = audioStream.getDeliveryMethod();
            final String mediaUrl = audioStream.getContent();
            if (audioStream.getFormat() == MediaFormat.OPUS) {
                assertSame(DeliveryMethod.HLS, deliveryMethod,
                        "Wrong delivery method for stream " + audioStream.getId() + ": "
                                + deliveryMethod);
                // Assert it's an OPUS 64 kbps media playlist URL which comes from an HLS
                // SoundCloud CDN
                ExtractorAsserts.assertContains("-hls-opus-media.sndcdn.com", mediaUrl);
                ExtractorAsserts.assertContains(".64.opus", mediaUrl);
            } else if (audioStream.getFormat() == MediaFormat.MP3) {
                if (deliveryMethod == DeliveryMethod.PROGRESSIVE_HTTP) {
                    // Assert it's a MP3 128 kbps media URL which comes from a progressive
                    // SoundCloud CDN
                    ExtractorAsserts.assertMatches(mp3CdnUrlPattern, mediaUrl);
                } else if (deliveryMethod == DeliveryMethod.HLS) {
                    // Assert it's a MP3 128 kbps media HLS playlist URL which comes from an HLS
                    // SoundCloud CDN
                    ExtractorAsserts.assertContains("-hls-media.sndcdn.com", mediaUrl);
                    ExtractorAsserts.assertContains(".128.mp3", mediaUrl);
                } else {
                    fail("Wrong delivery method for stream " + audioStream.getId() + ": "
                            + deliveryMethod);
                }
            }
        });
    }
}
