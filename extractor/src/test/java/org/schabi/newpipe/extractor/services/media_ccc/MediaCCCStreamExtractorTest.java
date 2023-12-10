package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCStreamExtractor;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.LocaleCompat;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

/**
 * Test {@link MediaCCCStreamExtractor}
 */
public class MediaCCCStreamExtractorTest {
    private static final String BASE_URL = "https://media.ccc.de/v/";

    public static class Gpn18Tmux extends DefaultStreamExtractorTest {
        private static final String ID = "gpn18-105-tmux-warum-ein-schwarzes-fenster-am-bildschirm-reicht";
        private static final String URL = BASE_URL + ID;
        private static StreamExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = MediaCCC.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        @Override public StreamExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return MediaCCC; }
        @Override public String expectedName() { return "tmux - Warum ein schwarzes Fenster am Bildschirm reicht"; }
        @Override public String expectedId() { return ID; }
        @Override public String expectedUrlContains() { return URL; }
        @Override public String expectedOriginalUrlContains() { return URL; }
        @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
        @Override public String expectedUploaderName() { return "gpn18"; }
        @Override public String expectedUploaderUrl() { return "https://media.ccc.de/c/gpn18"; }
        @Override public List<String> expectedDescriptionContains() { return Arrays.asList("SSH-Sessions", "\"Terminal Multiplexer\""); }
        @Override public long expectedLength() { return 3097; }
        @Override public long expectedViewCountAtLeast() { return 2380; }
        @Nullable @Override public String expectedUploadDate() { return "2018-05-11 00:00:00.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2018-05-11T02:00:00.000+02:00"; }
        @Override public long expectedLikeCountAtLeast() { return -1; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public boolean expectedHasRelatedItems() { return false; }
        @Override public boolean expectedHasSubtitles() { return false; }
        @Override public boolean expectedHasFrames() { return false; }
        @Override public List<String> expectedTags() { return Arrays.asList("gpn18", "105"); }
        @Override public int expectedStreamSegmentsCount() { return 0; }
        @Override public Locale expectedLanguageInfo() { return new Locale("de"); }

        @Override
        @Test
        public void testThumbnailUrl() throws Exception {
            super.testThumbnailUrl();
            assertEquals("https://static.media.ccc.de/media/events/gpn/gpn18/105-hd.jpg", extractor.getThumbnailUrl());
        }

        @Override
        @Test
        public void testUploaderAvatarUrl() throws Exception {
            super.testUploaderAvatarUrl();
            assertEquals("https://static.media.ccc.de/media/events/gpn/gpn18/logo.png", extractor.getUploaderAvatarUrl());
        }

        @Override
        @Test
        public void testVideoStreams() throws Exception {
            super.testVideoStreams();
            assertEquals(4, extractor.getVideoStreams().size());
        }

        @Override
        @Test
        public void testAudioStreams() throws Exception {
            super.testAudioStreams();
            final List<AudioStream> audioStreams = extractor.getAudioStreams();
            assertEquals(2, audioStreams.size());
            final Locale expectedLocale = LocaleCompat.forLanguageTag("deu");
            assertTrue(audioStreams.stream().allMatch(audioStream ->
                    Objects.equals(audioStream.getAudioLocale(), expectedLocale)));
        }
    }

    public static class _36c3PrivacyMessaging extends DefaultStreamExtractorTest {
        private static final String ID = "36c3-10565-what_s_left_for_private_messaging";
        private static final String URL = BASE_URL + ID;
        private static StreamExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = MediaCCC.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        @Override public StreamExtractor extractor() {
            return extractor;
        }
        @Override public StreamingService expectedService() {
            return MediaCCC;
        }
        @Override public String expectedName() {
            return "What's left for private messaging?";
        }
        @Override public String expectedId() {
            return ID;
        }
        @Override public String expectedUrlContains() { return URL; }
        @Override public String expectedOriginalUrlContains() { return URL; }
        @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
        @Override public String expectedUploaderName() { return "36c3"; }
        @Override public String expectedUploaderUrl() { return "https://media.ccc.de/c/36c3"; }
        @Override public List<String> expectedDescriptionContains() { return Arrays.asList("WhatsApp", "Signal"); }
        @Override public long expectedLength() { return 3603; }
        @Override public long expectedViewCountAtLeast() { return 2380; }
        @Nullable @Override public String expectedUploadDate() { return "2020-01-11 00:00:00.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2020-01-11T01:00:00.000+01:00"; }
        @Override public long expectedLikeCountAtLeast() { return -1; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public boolean expectedHasRelatedItems() { return false; }
        @Override public boolean expectedHasSubtitles() { return false; }
        @Override public boolean expectedHasFrames() { return false; }
        @Override public List<String> expectedTags() { return Arrays.asList("36c3", "10565", "2019", "Security", "Main"); }

        @Override
        @Test
        public void testThumbnailUrl() throws Exception {
            super.testThumbnailUrl();
            assertEquals("https://static.media.ccc.de/media/congress/2019/10565-hd.jpg", extractor.getThumbnailUrl());
        }

        @Override
        @Test
        public void testUploaderAvatarUrl() throws Exception {
            super.testUploaderAvatarUrl();
            assertEquals("https://static.media.ccc.de/media/congress/2019/logo.png", extractor.getUploaderAvatarUrl());
        }

        @Override
        @Test
        public void testVideoStreams() throws Exception {
            super.testVideoStreams();
            assertEquals(8, extractor.getVideoStreams().size());
        }

        @Override
        @Test
        public void testAudioStreams() throws Exception {
            super.testAudioStreams();
            final List<AudioStream> audioStreams = extractor.getAudioStreams();
            assertEquals(2, audioStreams.size());
            final Locale expectedLocale = LocaleCompat.forLanguageTag("eng");
            assertTrue(audioStreams.stream().allMatch(audioStream ->
                    Objects.equals(audioStream.getAudioLocale(), expectedLocale)));
        }

        @Override
        public Locale expectedLanguageInfo() {
            return new Locale("en");
        }
    }
}
