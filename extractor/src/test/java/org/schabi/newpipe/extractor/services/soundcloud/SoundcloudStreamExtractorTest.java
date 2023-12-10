package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.GeographicRestrictionException;
import org.schabi.newpipe.extractor.exceptions.SoundCloudGoPlusContentException;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

public class SoundcloudStreamExtractorTest {
    private static final String SOUNDCLOUD = "https://soundcloud.com/";

    public static class SoundcloudGeoRestrictedTrack extends DefaultStreamExtractorTest {
        private static final String ID = "one-touch";
        private static final String UPLOADER = SOUNDCLOUD + "jessglynne";
        private static final int TIMESTAMP = 0;
        private static final String URL = UPLOADER + "/" + ID + "#t=" + TIMESTAMP;
        private static StreamExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = SoundCloud.getStreamExtractor(URL);
            try {
                extractor.fetchPage();
            } catch (final GeographicRestrictionException e) {
                // expected
            }
        }

        @Override public StreamExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return SoundCloud; }
        @Override public String expectedName() { return "Jess Glynne & Jax Jones - One Touch"; }
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
        @Nullable @Override public String expectedTextualUploadDate() { return "2019-05-16 16:28:45"; }
        @Override public long expectedLikeCountAtLeast() { return 600; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public boolean expectedHasAudioStreams() { return false; }
        @Override public boolean expectedHasVideoStreams() { return false; }
        @Override public boolean expectedHasSubtitles() { return false; }
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

    public static class SoundcloudGoPlusTrack extends DefaultStreamExtractorTest {
        private static final String ID = "places";
        private static final String UPLOADER = SOUNDCLOUD + "martinsolveig";
        private static final int TIMESTAMP = 0;
        private static final String URL = UPLOADER + "/" + ID + "#t=" + TIMESTAMP;
        private static StreamExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = SoundCloud.getStreamExtractor(URL);
            try {
                extractor.fetchPage();
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

        @Override public StreamExtractor extractor() { return extractor; }
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
        @Nullable @Override public String expectedTextualUploadDate() { return "2016-11-11 01:16:37"; }
        @Override public long expectedLikeCountAtLeast() { return 7350; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public boolean expectedHasAudioStreams() { return false; }
        @Override public boolean expectedHasVideoStreams() { return false; }
        @Override public boolean expectedHasRelatedItems() { return true; }
        @Override public boolean expectedHasSubtitles() { return false; }
        @Override public boolean expectedHasFrames() { return false; }
        @Override public int expectedStreamSegmentsCount() { return 0; }
        @Override public String expectedLicence() { return "all-rights-reserved"; }
        @Override public String expectedCategory() { return "Dance"; }
    }

    public static class CreativeCommonsPlaysWellWithOthers extends DefaultStreamExtractorTest {
        private static final String ID = "plays-well-with-others-ep-2-what-do-an-army-of-ants-and-an-online-encyclopedia-have-in-common";
        private static final String UPLOADER = SOUNDCLOUD + "wearecc";
        private static final int TIMESTAMP = 69;
        private static final String URL = UPLOADER + "/" + ID + "#t=" + TIMESTAMP;
        private static StreamExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = SoundCloud.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        @Override public StreamExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return SoundCloud; }
        @Override public String expectedName() { return "Plays Well with Others, Ep 2: What Do an Army of Ants and an Online Encyclopedia Have in Common?"; }
        @Override public String expectedId() { return "597253485"; }
        @Override public String expectedUrlContains() { return UPLOADER + "/" + ID; }
        @Override public String expectedOriginalUrlContains() { return URL; }

        @Override public StreamType expectedStreamType() { return StreamType.AUDIO_STREAM; }
        @Override public String expectedUploaderName() { return "Creative Commons"; }
        @Override public String expectedUploaderUrl() { return UPLOADER; }
        @Override public List<String> expectedDescriptionContains() { return Arrays.asList("Stigmergy is a mechanism of indirect coordination",
                "All original content in Plays Well with Others is available under a Creative Commons BY license."); }
        @Override public long expectedLength() { return 1400; }
        @Override public long expectedTimestamp() { return TIMESTAMP; }
        @Override public long expectedViewCountAtLeast() { return 27000; }
        @Nullable @Override public String expectedUploadDate() { return "2019-03-28 13:36:18.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2019-03-28 13:36:18"; }
        @Override public long expectedLikeCountAtLeast() { return 25; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public boolean expectedHasVideoStreams() { return false; }
        @Override public boolean expectedHasSubtitles() { return false; }
        @Override public boolean expectedHasFrames() { return false; }
        @Override public int expectedStreamSegmentsCount() { return 0; }
        @Override public String expectedLicence() { return "cc-by"; }
        @Override public String expectedCategory() { return "Podcast"; }
        @Override public List<String> expectedTags() {
            return Arrays.asList("ants", "collaboration", "creative commons", "stigmergy", "storytelling", "wikipedia");
        }

        @Override
        @Test
        public void testAudioStreams() throws Exception {
            super.testAudioStreams();
            final List<AudioStream> audioStreams = extractor.getAudioStreams();
            assertEquals(2, audioStreams.size());
            audioStreams.forEach(audioStream -> {
                final DeliveryMethod deliveryMethod = audioStream.getDeliveryMethod();
                final String mediaUrl = audioStream.getContent();
                if (audioStream.getFormat() == MediaFormat.OPUS) {
                    // Assert that it's an OPUS 64 kbps media URL with a single range which comes
                    // from an HLS SoundCloud CDN
                    ExtractorAsserts.assertContains("-hls-opus-media.sndcdn.com", mediaUrl);
                    ExtractorAsserts.assertContains(".64.opus", mediaUrl);
                    assertSame(DeliveryMethod.HLS, deliveryMethod,
                            "Wrong delivery method for stream " + audioStream.getId() + ": "
                                    + deliveryMethod);
                } else if (audioStream.getFormat() == MediaFormat.MP3) {
                    // Assert that it's a MP3 128 kbps media URL which comes from a progressive
                    // SoundCloud CDN
                    ExtractorAsserts.assertContains("-media.sndcdn.com/bKOA7Pwbut93.128.mp3",
                            mediaUrl);
                    assertSame(DeliveryMethod.PROGRESSIVE_HTTP, deliveryMethod,
                            "Wrong delivery method for stream " + audioStream.getId() + ": "
                                    + deliveryMethod);
                }
            });
        }
    }
}
