package org.schabi.newpipe.extractor.services.soundcloud;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.GeographicRestrictionException;
import org.schabi.newpipe.extractor.exceptions.SoundCloudGoPlusContentException;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.StreamingProtocol;
import org.schabi.newpipe.extractor.stream.deliverysource.DeliverySource;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.HttpDeliverySource;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.UriObject;
import org.schabi.newpipe.extractor.stream.interfaces.AudioStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseAudioStream;
import org.schabi.newpipe.extractor.stream.mediaformat.AudioMediaFormat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoundcloudStreamExtractorTest {
    private static final String SOUNDCLOUD = "https://soundcloud.com/";

    @Nested
    class SoundcloudGeoRestrictedTrack extends DefaultStreamExtractorTest {
        private static final String ID = "one-touch";
        private static final String UPLOADER = SOUNDCLOUD + "jessglynne";
        private static final int TIMESTAMP = 0;
        private static final String URL = UPLOADER + "/" + ID + "#t=" + TIMESTAMP;

        @Override
        protected StreamExtractor createExtractor() throws Exception {
            return SoundCloud.getStreamExtractor(URL);
        }

        @Override
        protected void fetchExtractor(final StreamExtractor extractor) throws Exception {
            try {
                super.fetchExtractor(extractor);
            } catch (final GeographicRestrictionException e) {
                // expected
            }
        }

        @Override public StreamingService expectedService() { return SoundCloud; }
        @Override public String expectedName() { return "One Touch"; }
        @Override public String expectedId() { return "621612588"; }
        @Override public String expectedUrlContains() { return UPLOADER + "/" + ID; }
        @Override public String expectedOriginalUrlContains() { return URL; }

        @Override public StreamType expectedStreamType() { return StreamType.AUDIO_STREAM; }
        @Override public String expectedUploaderName() { return "Jess Glynne"; }
        @Override public String expectedUploaderUrl() { return UPLOADER; }
        @Override public boolean expectedUploaderVerified() { return true; }
        @Override public boolean expectedDescriptionIsEmpty() { return true; }
        @Override public List<String> expectedDescriptionContains() { return Collections.emptyList(); }
        @Override public long expectedLength() { return 197; }
        @Override public long expectedTimestamp() { return TIMESTAMP; }
        @Override public long expectedViewCountAtLeast() { return 43000; }
        @Nullable @Override public String expectedUploadDate() { return "2019-05-16 16:28:45.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2019-05-16T16:28:45Z"; }
        @Override public long expectedLikeCountAtLeast() { return 600; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public boolean expectedHasAudioStreams() { return false; }
        @Override public boolean expectedHasVideoStreams() { return false; }
        @Override public boolean expectedHasSubtitles() { return false; }
        @Override public boolean expectedHasMuxedStreams() { return false; }
        @Override public boolean expectedHasFrames() { return false; }
        @Override public int expectedStreamSegmentsCount() { return 0; }
        @Override public String expectedLicence() { return "all-rights-reserved"; }
        @Override public String expectedCategory() { return "Pop"; }

        @Test
        @Override
        @Disabled("Unreliable, sometimes it has related items, sometimes it does not")
        public void testRelatedItems() throws Exception {
            super.testRelatedItems();
        }
    }

    @Nested
    class SoundcloudGoPlusTrack extends DefaultStreamExtractorTest {
        private static final String ID = "places";
        private static final String UPLOADER = SOUNDCLOUD + "martinsolveig";
        private static final int TIMESTAMP = 0;
        private static final String URL = UPLOADER + "/" + ID + "#t=" + TIMESTAMP;

        @Override
        protected StreamExtractor createExtractor() throws Exception {
            return SoundCloud.getStreamExtractor(URL);
        }

        @Override
        protected void fetchExtractor(final StreamExtractor extractor) throws Exception {
            try {
                super.fetchExtractor(extractor);
            } catch (final SoundCloudGoPlusContentException e) {
                // expected
            }
        }

        @Override
        @Test
        @Disabled("Unreliable, sometimes it has related items, sometimes it does not. See " +
                "https://github.com/TeamNewPipe/NewPipeExtractor/runs/2280013723#step:5:263 " +
                "https://github.com/TeamNewPipe/NewPipeExtractor/pull/601")
        public void testRelatedItems() throws Exception {
            super.testRelatedItems();
        }

        @Override public StreamingService expectedService() { return SoundCloud; }
        @Override public String expectedName() { return "Places (feat. Ina Wroldsen)"; }
        @Override public String expectedId() { return "292479564"; }
        @Override public String expectedUrlContains() { return UPLOADER + "/" + ID; }
        @Override public String expectedOriginalUrlContains() { return URL; }

        @Override public StreamType expectedStreamType() { return StreamType.AUDIO_STREAM; }
        @Override public String expectedUploaderName() { return "martinsolveig"; }
        @Override public String expectedUploaderUrl() { return UPLOADER; }
        @Override public boolean expectedUploaderVerified() { return true; }
        @Override public boolean expectedDescriptionIsEmpty() { return true; }
        @Override public List<String> expectedDescriptionContains() { return Collections.emptyList(); }
        @Override public long expectedLength() { return 30; }
        @Override public long expectedTimestamp() { return TIMESTAMP; }
        @Override public long expectedViewCountAtLeast() { return 386000; }
        @Nullable @Override public String expectedUploadDate() { return "2016-11-11 01:16:37.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2016-11-11T01:16:37Z"; }
        @Override public long expectedLikeCountAtLeast() { return 7350; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public boolean expectedHasAudioStreams() { return false; }
        @Override public boolean expectedHasVideoStreams() { return false; }
        @Override public boolean expectedHasSubtitles() { return false; }
        @Override public boolean expectedHasMuxedStreams() { return false; }
        @Override public boolean expectedHasFrames() { return false; }
        @Override public int expectedStreamSegmentsCount() { return 0; }
        @Override public String expectedLicence() { return "all-rights-reserved"; }
        @Override public String expectedCategory() { return "Dance"; }
    }

    @Nested
    class CreativeCommonsOpenMindsEp21 extends DefaultStreamExtractorTest {
        private static final String ID = "open-minds-ep-21-dr-beth-harris-and-dr-steven-zucker-of-smarthistory";
        private static final String UPLOADER = SOUNDCLOUD + "wearecc";
        private static final int TIMESTAMP = 69;
        private static final String URL = UPLOADER + "/" + ID + "#t=" + TIMESTAMP;

        @Override
        protected StreamExtractor createExtractor() throws Exception {
            return SoundCloud.getStreamExtractor(URL);
        }

        @Override public StreamingService expectedService() { return SoundCloud; }
        @Override public String expectedName() { return "Open Minds, Ep 21: Dr. Beth Harris and Dr. Steven Zucker of Smarthistory"; }
        @Override public String expectedId() { return "1356023209"; }
        @Override public String expectedUrlContains() { return UPLOADER + "/" + ID; }
        @Override public String expectedOriginalUrlContains() { return URL; }

        @Override public StreamType expectedStreamType() { return StreamType.AUDIO_STREAM; }
        @Override public String expectedUploaderName() { return "Creative Commons"; }
        @Override public String expectedUploaderUrl() { return UPLOADER; }
        @Override public List<String> expectedDescriptionContains() {
            return Arrays.asList("Smarthistory is a center for public art history",
                "experts who want to share their knowledge with learners around the world",
                "Available for use under the CC BY 3.0 license"); }
        @Override public long expectedLength() { return 1500; }
        @Override public long expectedTimestamp() { return TIMESTAMP; }
        @Override public long expectedViewCountAtLeast() { return 15000; }
        @Nullable @Override public String expectedUploadDate() { return "2022-10-03 18:49:49.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2022-10-03T18:49:49Z"; }
        @Override public long expectedLikeCountAtLeast() { return 10; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public boolean expectedHasRelatedItems() { return false; }
        @Override public boolean expectedHasVideoStreams() { return false; }
        @Override public boolean expectedHasSubtitles() { return false; }
        @Override public boolean expectedHasMuxedStreams() { return false; }
        @Override public boolean expectedHasFrames() { return false; }
        @Override public int expectedStreamSegmentsCount() { return 0; }
        @Override public String expectedLicence() { return "cc-by"; }

        @Override
        protected void testAudioStreams(@Nonnull final List<BaseAudioStream> audioStreams) {
            super.testAudioStreams(audioStreams);

            // 2 MP3 streams (1 progressive, 1 HLS) and 1 OPUS
            assertEquals(1, audioStreams.size());
            final List<AudioStream> castedAudioStreams = audioStreams.stream()
                    .filter(AudioStream.class::isInstance)
                    .map(AudioStream.class::cast)
                    .toList();
            // All streams should be an instance of AudioStream
            assertEquals(1, castedAudioStreams.size());
            castedAudioStreams.forEach(castedAudioStream -> {
                final StreamingProtocol streamingProtocol =
                        castedAudioStream.getStreamingProtocol();
                final AudioMediaFormat mediaFormat = castedAudioStream.getMediaFormat();

                final DeliverySource deliverySource = castedAudioStream.getDeliverySource();
                final String mediaUrl;
                if (deliverySource instanceof HttpDeliverySource httpDeliverySource) {
                    final UriObject uriObject = httpDeliverySource.getUri();
                    mediaUrl = uriObject.getUri();
                    assertEquals(UriObject.EXPIRATION_TIMESTAMP_UNKNOWN,
                            uriObject.getExpirationTimestamp());
                    assertNotNull(uriObject.getRefresher());
                } else {
                    throw new AssertionError("Unexpected delivery source: " + deliverySource);
                }

                if (mediaFormat == AudioMediaFormat.OPUS) {
                    assertEquals(StreamingProtocol.PROGRESSIVE, streamingProtocol,
                            "Wrong streaming protocol for OPUS stream: " + castedAudioStream.getId()
                                    + ": " + streamingProtocol);
                    // Assert it's an OPUS 64 kbps media playlist URL which comes from an HLS
                    // SoundCloud CDN
                    ExtractorAsserts.assertContains("-hls-opus-media.sndcdn.com", mediaUrl);
                    ExtractorAsserts.assertContains(".64.opus", mediaUrl);
                } else if (mediaFormat == AudioMediaFormat.MP3) {
                    if (streamingProtocol == StreamingProtocol.PROGRESSIVE) {
                        // Assert it's a MP3 128 kbps media URL which comes from a progressive
                        // SoundCloud CDN
                        ExtractorAsserts.assertContains("-media.sndcdn.com/cyaz0oXJYbdt.128.mp3",
                                mediaUrl);
                    } else if (streamingProtocol == StreamingProtocol.HLS) {
                        // Assert it's a MP3 128 kbps media HLS playlist URL which comes from an HLS
                        // SoundCloud CDN
                        ExtractorAsserts.assertContains("-hls-media.sndcdn.com", mediaUrl);
                        ExtractorAsserts.assertContains(".128.mp3", mediaUrl);
                    } else {
                        fail("Wrong streaming protocol for stream: " + castedAudioStream.getId()
                                + ": " + streamingProtocol);
                    }
                } else if (mediaFormat == AudioMediaFormat.M4A) {
                    assertSame(StreamingProtocol.HLS, streamingProtocol,
                            "Wrong streaming protocol for stream " + castedAudioStream.getId() + ": "
                                    + streamingProtocol);
                    // Assert it's an AAC 96 kbps HLS media playlist URL
                    ExtractorAsserts.assertContains("/aac_96k/", mediaUrl);
                    ExtractorAsserts.assertContains("playlist.m3u8", mediaUrl);
                }
            });
        }
    }

    @Nested
    class SmallTrackId extends DefaultStreamExtractorTest {
        private static final String ID = "technicality";
        private static final String UPLOADER = SOUNDCLOUD + "garrett-cameron-1";
        private static final int TIMESTAMP = 34;
        private static final String URL = UPLOADER + "/" + ID + "#t=" + TIMESTAMP;

        @Override
        protected StreamExtractor createExtractor() throws Exception {
            return SoundCloud.getStreamExtractor(URL);
        }

        @Override public StreamingService expectedService() { return SoundCloud; }
        @Override public String expectedName() { return "Technicality"; }
        @Override public String expectedId() { return "135535700"; }
        @Override public String expectedUrlContains() { return UPLOADER + "/" + ID; }
        @Override public String expectedOriginalUrlContains() { return URL; }

        @Override public StreamType expectedStreamType() { return StreamType.AUDIO_STREAM; }
        @Override public String expectedUploaderName() { return "Samuel Cameron (ZVW)"; }
        @Override public String expectedUploaderUrl() { return UPLOADER; }
        @Override public boolean expectedDescriptionIsEmpty() { return true; }
        @Override public List<String> expectedDescriptionContains() { return Collections.emptyList(); }
        @Override public long expectedLength() { return 182; }
        @Override public long expectedTimestamp() { return TIMESTAMP; }
        @Override public long expectedViewCountAtLeast() { return 16; }
        @Nullable @Override public String expectedUploadDate() { return "2014-02-18 21:25:01.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2014-02-18T21:25:01Z"; }
        @Override public long expectedLikeCountAtLeast() { return 1; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public boolean expectedHasVideoStreams() { return false; }
        @Override public boolean expectedHasSubtitles() { return false; }
        @Override public boolean expectedHasMuxedStreams() { return false; }
        @Override public boolean expectedHasFrames() { return false; }
        @Override public int expectedStreamSegmentsCount() { return 0; }
        @Override public String expectedLicence() { return "all-rights-reserved"; }
        @Override public String expectedCategory() { return "Electronic"; }
    }

    @Nested
    class LongTrackId extends DefaultStreamExtractorTest {
        private static final String ID = "hard-to-break";
        private static final String UPLOADER = SOUNDCLOUD + "blakewhiten";
        private static final int TIMESTAMP = 130;
        private static final String URL = UPLOADER + "/" + ID + "#t=" + TIMESTAMP;

        @Override
        protected StreamExtractor createExtractor() throws Exception {
            return SoundCloud.getStreamExtractor(URL);
        }

        @Override public StreamingService expectedService() { return SoundCloud; }
        @Override public String expectedName() { return "Hard to Break"; }
        @Override public String expectedId() { return "2167944333"; }
        @Override public String expectedUrlContains() { return UPLOADER + "/" + ID; }
        @Override public String expectedOriginalUrlContains() { return URL; }

        @Override public StreamType expectedStreamType() { return StreamType.AUDIO_STREAM; }
        @Override public String expectedUploaderName() { return "Blake Whiten"; }
        @Override public String expectedUploaderUrl() { return UPLOADER; }
        @Override public boolean expectedDescriptionIsEmpty() { return true; }
        @Override public List<String> expectedDescriptionContains() { return Collections.emptyList(); }
        @Override public long expectedLength() { return 201; }
        @Override public long expectedTimestamp() { return TIMESTAMP; }
        @Override public long expectedViewCountAtLeast() { return 120222; }
        @Nullable @Override public String expectedUploadDate() { return "2025-09-10 02:24:00.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2025-09-10T02:24:00Z"; }
        @Override public long expectedLikeCountAtLeast() { return 1970; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public boolean expectedHasVideoStreams() { return false; }
        @Override public boolean expectedHasAudioStreams() { return false; } // DRM-protected track
        @Override public boolean expectedHasSubtitles() { return false; }
        @Override public boolean expectedHasMuxedStreams() { return false; }
        @Override public boolean expectedHasFrames() { return false; }
        @Override public int expectedStreamSegmentsCount() { return 0; }
        @Override public String expectedLicence() { return "all-rights-reserved"; }
        @Override public String expectedCategory() { return "Country"; }
    }
}
