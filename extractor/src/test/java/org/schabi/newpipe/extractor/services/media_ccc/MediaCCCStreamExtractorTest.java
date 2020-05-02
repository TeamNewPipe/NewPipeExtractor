package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCStreamExtractor;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static junit.framework.TestCase.assertEquals;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

/**
 * Test {@link MediaCCCStreamExtractor}
 */
public class MediaCCCStreamExtractorTest {
    public static class Gpn18Tmux {
        private static MediaCCCStreamExtractor extractor;

        @BeforeClass
        public static void setUpClass() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());

            extractor = (MediaCCCStreamExtractor) MediaCCC.getStreamExtractor("https://media.ccc.de/v/gpn18-105-tmux-warum-ein-schwarzes-fenster-am-bildschirm-reicht");
            extractor.fetchPage();
        }

        @Test
        public void testServiceId() throws Exception {
            assertEquals(2, extractor.getServiceId());
        }

        @Test
        public void testName() throws Exception {
            assertEquals("tmux - Warum ein schwarzes Fenster am Bildschirm reicht", extractor.getName());
        }

        @Test
        public void testId() throws Exception {
            assertEquals("gpn18-105-tmux-warum-ein-schwarzes-fenster-am-bildschirm-reicht", extractor.getId());
        }

        @Test
        public void testUrl() throws Exception {
            assertIsSecureUrl(extractor.getUrl());
            assertEquals("https://media.ccc.de/public/events/gpn18-105-tmux-warum-ein-schwarzes-fenster-am-bildschirm-reicht", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws Exception {
            assertIsSecureUrl(extractor.getOriginalUrl());
            assertEquals("https://media.ccc.de/v/gpn18-105-tmux-warum-ein-schwarzes-fenster-am-bildschirm-reicht", extractor.getOriginalUrl());
        }

        @Test
        public void testThumbnail() throws Exception {
            assertIsSecureUrl(extractor.getThumbnailUrl());
            assertEquals("https://static.media.ccc.de/media/events/gpn/gpn18/105-hd.jpg", extractor.getThumbnailUrl());
        }

        @Test
        public void testUploaderName() throws Exception {
            assertEquals("gpn18", extractor.getUploaderName());
        }

        @Test
        public void testUploaderUrl() throws Exception {
            assertIsSecureUrl(extractor.getUploaderUrl());
            assertEquals("https://media.ccc.de/public/conferences/gpn18", extractor.getUploaderUrl());
        }

        @Test
        public void testUploaderAvatarUrl() throws Exception {
            assertIsSecureUrl(extractor.getUploaderAvatarUrl());
            assertEquals("https://static.media.ccc.de/media/events/gpn/gpn18/logo.png", extractor.getUploaderAvatarUrl());
        }

        @Test
        public void testVideoStreams() throws Exception {
            List<VideoStream> videoStreamList = extractor.getVideoStreams();
            assertEquals(4, videoStreamList.size());
            for (VideoStream stream : videoStreamList) {
                assertIsSecureUrl(stream.getUrl());
            }
        }

        @Test
        public void testAudioStreams() throws Exception {
            List<AudioStream> audioStreamList = extractor.getAudioStreams();
            assertEquals(2, audioStreamList.size());
            for (AudioStream stream : audioStreamList) {
                assertIsSecureUrl(stream.getUrl());
            }
        }

        @Test
        public void testGetTextualUploadDate() throws ParsingException {
            Assert.assertEquals("2018-05-11T02:00:00.000+02:00", extractor.getTextualUploadDate());
        }

        @Test
        public void testGetUploadDate() throws ParsingException, ParseException {
            final Calendar instance = Calendar.getInstance();
            instance.setTime(new SimpleDateFormat("yyyy-MM-dd").parse("2018-05-11"));
            assertEquals(instance, requireNonNull(extractor.getUploadDate()).date());
        }
    }

    public static class _36c3PrivacyMessaging {
        private static MediaCCCStreamExtractor extractor;

        @BeforeClass
        public static void setUpClass() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (MediaCCCStreamExtractor) MediaCCC.getStreamExtractor("https://media.ccc.de/v/36c3-10565-what_s_left_for_private_messaging");
            extractor.fetchPage();
        }

        @Test
        public void testName() throws Exception {
            assertEquals("What's left for private messaging?", extractor.getName());
        }

        @Test
        public void testId() throws Exception {
            assertEquals("36c3-10565-what_s_left_for_private_messaging", extractor.getId());
        }

        @Test
        public void testUrl() throws Exception {
            assertIsSecureUrl(extractor.getUrl());
            assertEquals("https://media.ccc.de/public/events/36c3-10565-what_s_left_for_private_messaging", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws Exception {
            assertIsSecureUrl(extractor.getOriginalUrl());
            assertEquals("https://media.ccc.de/v/36c3-10565-what_s_left_for_private_messaging", extractor.getOriginalUrl());
        }

        @Test
        public void testThumbnail() throws Exception {
            assertIsSecureUrl(extractor.getThumbnailUrl());
            assertEquals("https://static.media.ccc.de/media/congress/2019/10565-hd.jpg", extractor.getThumbnailUrl());
        }

        @Test
        public void testUploaderName() throws Exception {
            assertEquals("36c3", extractor.getUploaderName());
        }

        @Test
        public void testUploaderUrl() throws Exception {
            assertIsSecureUrl(extractor.getUploaderUrl());
            assertEquals("https://media.ccc.de/public/conferences/36c3", extractor.getUploaderUrl());
        }

        @Test
        public void testUploaderAvatarUrl() throws Exception {
            assertIsSecureUrl(extractor.getUploaderAvatarUrl());
            assertEquals("https://static.media.ccc.de/media/congress/2019/logo.png", extractor.getUploaderAvatarUrl());
        }

        @Test
        public void testVideoStreams() throws Exception {
            List<VideoStream> videoStreamList = extractor.getVideoStreams();
            assertEquals(8, videoStreamList.size());
            for (VideoStream stream : videoStreamList) {
                assertIsSecureUrl(stream.getUrl());
            }
        }

        @Test
        public void testAudioStreams() throws Exception {
            List<AudioStream> audioStreamList = extractor.getAudioStreams();
            assertEquals(2, audioStreamList.size());
            for (AudioStream stream : audioStreamList) {
                assertIsSecureUrl(stream.getUrl());
            }
        }

        @Test
        public void testGetTextualUploadDate() throws ParsingException {
            Assert.assertEquals("2020-01-11T01:00:00.000+01:00", extractor.getTextualUploadDate());
        }

        @Test
        public void testGetUploadDate() throws ParsingException, ParseException {
            final Calendar instance = Calendar.getInstance();
            instance.setTime(new SimpleDateFormat("yyyy-MM-dd").parse("2020-01-11"));
            assertEquals(instance, requireNonNull(extractor.getUploadDate()).date());
        }
    }
}
