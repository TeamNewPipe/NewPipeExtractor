package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.GeographicRestrictionException;
import org.schabi.newpipe.extractor.exceptions.SoundCloudGoPlusContentException;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.services.ParameterisedDefaultSoundcloudStreamExtractorTest;
import org.schabi.newpipe.extractor.services.testcases.SoundcloudStreamExtractorTestCase;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

public class SoundcloudStreamExtractorTest {
    private static final String SOUNDCLOUD = "https://soundcloud.com/";

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SoundcloudGeoRestrictedTrack extends DefaultStreamExtractorTest {
        private static final String ID = "one-touch";
        private static final String UPLOADER = SOUNDCLOUD + "jessglynne";
        private static final int TIMESTAMP = 0;
        private static final String URL = UPLOADER + "/" + ID + "#t=" + TIMESTAMP;
        private StreamExtractor extractor;

        @BeforeAll
        public void setUp() throws Exception {
            if (extractor != null) {
                throw new IllegalStateException("extractor already initialized before BeforeAll");
            }
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

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SoundcloudGoPlusTrack extends DefaultStreamExtractorTest {
        private static final String ID = "places";
        private static final String UPLOADER = SOUNDCLOUD + "martinsolveig";
        private static final int TIMESTAMP = 0;
        private static final String URL = UPLOADER + "/" + ID + "#t=" + TIMESTAMP;
        private StreamExtractor extractor;

        @BeforeAll
        public void setUp() throws Exception {
            if (extractor != null) {
                throw new IllegalStateException("extractor already initialized before BeforeAll");
            }
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
        @Override public boolean expectedHasSubtitles() { return false; }
        @Override public boolean expectedHasFrames() { return false; }
        @Override public int expectedStreamSegmentsCount() { return 0; }
        @Override public String expectedLicence() { return "all-rights-reserved"; }
        @Override public String expectedCategory() { return "Dance"; }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SoundcloudTrackTest1 extends ParameterisedDefaultSoundcloudStreamExtractorTest {
        public SoundcloudTrackTest1() {
            super(
                SoundcloudStreamExtractorTestCase.builder()
                    .url("https://soundcloud.com/user-904087338/nether#t=45")
                    .id("2057071056")
                    .name("Nether")
                    .uploaderName("Ambient Ghost")
                    .uploadDate("2025-03-18 12:19:19.000")
                    .textualUploadDate("2025-03-18 12:19:19")
                    .length(145)
                    .licence("all-rights-reserved")
                    .descriptionIsEmpty(true)
                    .viewCountAtLeast(1029)
                    .likeCountAtLeast(12)
                .build()
            );
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SoundcloudTrackTest2 extends ParameterisedDefaultSoundcloudStreamExtractorTest {
        public SoundcloudTrackTest2() {
            super(
            SoundcloudStreamExtractorTestCase.builder()
                .url("https://soundcloud.com/kaleidocollective/2subtact-splinter")
                .id("230211123")
                .name("Subtact - Splinter")
                .uploaderVerified(true)
                .uploaderName("Kaleido")
                .uploadDate("2015-10-26 20:55:30.000")
                .textualUploadDate("2015-10-26 20:55:30")
                .length(225)
                .licence("all-rights-reserved")
                .descriptionIsEmpty(false)
                .addDescriptionContains("follow @subtact",
                                        "-twitter:",
                                        "twitter.com/Subtact",
                                        "-facebook:",
                                        "www.facebook.com/subtact?fref=ts")
                .viewCountAtLeast(157874)
                .likeCountAtLeast(3142)
                .category("ʕ•ᴥ•ʔ")
            .build()
            );
        }

    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SoundcloudTrackTest3 extends ParameterisedDefaultSoundcloudStreamExtractorTest {
        public SoundcloudTrackTest3() {
            super(SoundcloudStreamExtractorTestCase.builder()
                    .url("https://soundcloud.com/wearecc/open-minds-ep-21-dr-beth-harris-and-dr-steven-zucker-of-smarthistory")
                    .id("1356023209")
                    .name("Open Minds, Ep 21: Dr. Beth Harris and Dr. Steven Zucker of Smarthistory")
                    .uploaderName("Creative Commons")
                    .uploadDate("2022-10-03 18:49:49.000")
                    .textualUploadDate("2022-10-03 18:49:49")
                    .hasRelatedItems(false)
                    .length(1500)
                    .licence("cc-by")
                    .descriptionIsEmpty(false)
                    .addDescriptionContains("On this episode, we're joined by art historians",
                                            "Follow Smarthistory on Twitter: https://twitter.com/Smarthistory",
                                            "Open Minds … from Creative Commons is licensed to the public under CC BY",
                                            "(https://creativecommons.org/licenses/by/4.0/)")
                    .viewCountAtLeast(15584)
                    .likeCountAtLeast(14)
                .build()
            );
        }

    }
}
