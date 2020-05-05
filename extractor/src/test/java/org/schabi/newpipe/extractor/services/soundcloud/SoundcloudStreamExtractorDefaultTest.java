package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudStreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

/**
 * Test for {@link StreamExtractor}
 */
public class SoundcloudStreamExtractorDefaultTest {

    public static class LilUziVertDoWhatIWant {
        private static SoundcloudStreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (SoundcloudStreamExtractor) SoundCloud.getStreamExtractor("https://soundcloud.com/liluzivert/do-what-i-want-produced-by-maaly-raw-don-cannon");
            extractor.fetchPage();
        }

        @Test
        public void testGetInvalidTimeStamp() throws ParsingException {
            assertTrue(extractor.getTimeStamp() + "",
                    extractor.getTimeStamp() <= 0);
        }

        @Test
        public void testGetValidTimeStamp() throws IOException, ExtractionException {
            StreamExtractor extractor = SoundCloud.getStreamExtractor("https://soundcloud.com/liluzivert/do-what-i-want-produced-by-maaly-raw-don-cannon#t=69");
            assertEquals("69", extractor.getTimeStamp() + "");
        }

        @Test
        public void testGetTitle() throws ParsingException {
            assertEquals("Do What I Want [Produced By Maaly Raw + Don Cannon]", extractor.getName());
        }

        @Test
        public void testGetDescription() throws ParsingException {
            assertEquals("The Perfect LUV Tape®️", extractor.getDescription().getContent());
        }

        @Test
        public void testGetUploaderName() throws ParsingException {
            assertEquals("LIL UZI VERT", extractor.getUploaderName());
        }

        @Test
        public void testGetLength() throws ParsingException {
            assertEquals(175, extractor.getLength());
        }

        @Test
        public void testGetViewCount() throws ParsingException {
            assertTrue(Long.toString(extractor.getViewCount()),
                    extractor.getViewCount() > 44227978);
        }

        @Test
        public void testGetTextualUploadDate() throws ParsingException {
            Assert.assertEquals("2016-07-31 18:18:07", extractor.getTextualUploadDate());
        }

        @Test
        public void testGetUploadDate() throws ParsingException, ParseException {
            final Calendar instance = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss +0000");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            instance.setTime(sdf.parse("2016/07/31 18:18:07 +0000"));
            assertEquals(instance, requireNonNull(extractor.getUploadDate()).date());
        }

        @Test
        public void testGetUploaderUrl() throws ParsingException {
            assertIsSecureUrl(extractor.getUploaderUrl());
            assertEquals("https://soundcloud.com/liluzivert", extractor.getUploaderUrl());
        }

        @Test
        public void testGetThumbnailUrl() throws ParsingException {
            assertIsSecureUrl(extractor.getThumbnailUrl());
        }

        @Test
        public void testGetUploaderAvatarUrl() throws ParsingException {
            assertIsSecureUrl(extractor.getUploaderAvatarUrl());
        }

        @Test
        public void testGetAudioStreams() throws IOException, ExtractionException {
            assertFalse(extractor.getAudioStreams().isEmpty());
        }

        @Test
        public void testStreamType() throws ParsingException {
            assertTrue(extractor.getStreamType() == StreamType.AUDIO_STREAM);
        }

        @Test
        public void testGetRelatedVideos() throws ExtractionException, IOException {
            StreamInfoItemsCollector relatedVideos = extractor.getRelatedStreams();
            assertFalse(relatedVideos.getItems().isEmpty());
            assertTrue(relatedVideos.getErrors().isEmpty());
        }

        @Test
        public void testGetSubtitlesListDefault() throws IOException, ExtractionException {
            // Video (/view?v=YQHsXMglC9A) set in the setUp() method has no captions => null
            assertTrue(extractor.getSubtitlesDefault().isEmpty());
        }

        @Test
        public void testGetSubtitlesList() throws IOException, ExtractionException {
            // Video (/view?v=YQHsXMglC9A) set in the setUp() method has no captions => null
            assertTrue(extractor.getSubtitlesDefault().isEmpty());
        }
    }

    public static class ContentNotSupported {
        @BeforeClass
        public static void setUp() {
            NewPipe.init(DownloaderTestImpl.getInstance());
        }

        @Test(expected = ContentNotSupportedException.class)
        public void hlsAudioStream() throws Exception {
            final StreamExtractor extractor =
                    SoundCloud.getStreamExtractor("https://soundcloud.com/dualipa/cool");
            extractor.fetchPage();
            extractor.getAudioStreams();
        }

        @Test(expected = ContentNotSupportedException.class)
        public void bothHlsAndOpusAudioStreams() throws Exception {
            final StreamExtractor extractor =
                    SoundCloud.getStreamExtractor("https://soundcloud.com/lil-baby-4pf/no-sucker");
            extractor.fetchPage();
            extractor.getAudioStreams();
        }
    }
}

