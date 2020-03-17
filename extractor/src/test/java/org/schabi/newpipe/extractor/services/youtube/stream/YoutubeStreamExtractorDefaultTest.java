package org.schabi.newpipe.extractor.services.youtube.stream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.stream.Frameset;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

/*
 * Created by Christian Schabesberger on 30.12.15.
 *
 * Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
 * YoutubeVideoExtractorDefault.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Test for {@link StreamExtractor}
 */
public class YoutubeStreamExtractorDefaultTest {

    public static class NotAvailable {
        @BeforeClass
        public static void setUp() {
            NewPipe.init(DownloaderTestImpl.getInstance());
        }

        @Test(expected = ContentNotAvailableException.class)
        public void nonExistentFetch() throws Exception {
            final StreamExtractor extractor =
                    YouTube.getStreamExtractor("https://www.youtube.com/watch?v=don-t-exist");
            extractor.fetchPage();
        }

        @Test(expected = ParsingException.class)
        public void invalidId() throws Exception {
            final StreamExtractor extractor =
                    YouTube.getStreamExtractor("https://www.youtube.com/watch?v=INVALID_ID_INVALID_ID");
            extractor.fetchPage();
        }
    }

    /**
     * Test for {@link StreamExtractor}
     */
    public static class AdeleHello {
        private static YoutubeStreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeStreamExtractor) YouTube
                    .getStreamExtractor("https://www.youtube.com/watch?v=YQHsXMglC9A");
            extractor.fetchPage();
        }

        @Test
        public void testGetInvalidTimeStamp() throws ParsingException {
            assertTrue(extractor.getTimeStamp() + "",
                    extractor.getTimeStamp() <= 0);
        }

        @Test
        public void testGetValidTimeStamp() throws ExtractionException {
            StreamExtractor extractor = YouTube.getStreamExtractor("https://youtu.be/FmG385_uUys?t=174");
            assertEquals(extractor.getTimeStamp() + "", "174");
        }

        @Test
        public void testGetTitle() throws ParsingException {
            assertFalse(extractor.getName().isEmpty());
        }

        @Test
        public void testGetDescription() throws ParsingException {
            assertNotNull(extractor.getDescription());
            assertFalse(extractor.getDescription().getContent().isEmpty());
        }

        @Test
        public void testGetFullLinksInDescription() throws ParsingException {
            assertTrue(extractor.getDescription().getContent().contains("http://adele.com"));
        }

        @Test
        public void testGetUploaderName() throws ParsingException {
            assertNotNull(extractor.getUploaderName());
            assertFalse(extractor.getUploaderName().isEmpty());
        }


        @Test
        public void testGetLength() throws ParsingException {
            assertEquals(367, extractor.getLength());
        }

        @Test
        public void testGetViewCount() throws ParsingException {
            Long count = extractor.getViewCount();
            assertTrue(Long.toString(count), count >= /* specific to that video */ 1220025784);
        }

        @Test
        public void testGetTextualUploadDate() throws ParsingException {
            Assert.assertEquals("2015-10-22", extractor.getTextualUploadDate());
        }

        @Test
        public void testGetUploadDate() throws ParsingException, ParseException {
            final Calendar instance = Calendar.getInstance();
            instance.setTime(new SimpleDateFormat("yyyy-MM-dd").parse("2015-10-22"));
            assertEquals(instance, requireNonNull(extractor.getUploadDate()).date());
        }

        @Test
        public void testGetUploaderUrl() throws ParsingException {
            String url = extractor.getUploaderUrl();
            if (!url.equals("https://www.youtube.com/channel/UCsRM0YB_dabtEPGPTKo-gcw") &&
                    !url.equals("https://www.youtube.com/channel/UComP_epzeKzvBX156r6pm1Q")) {
                fail("Uploader url is neither the music channel one nor the Vevo one");
            }
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
        public void testGetAudioStreams() throws ExtractionException {
            assertFalse(extractor.getAudioStreams().isEmpty());
        }

        @Test
        public void testGetVideoStreams() throws ExtractionException {
            for (VideoStream s : extractor.getVideoStreams()) {
                assertIsSecureUrl(s.url);
                assertTrue(s.resolution.length() > 0);
                assertTrue(Integer.toString(s.getFormatId()),
                        0 <= s.getFormatId() && s.getFormatId() <= 0x100);
            }
        }

        @Test
        public void testStreamType() throws ParsingException {
            assertTrue(extractor.getStreamType() == StreamType.VIDEO_STREAM);
        }

        @Test
        public void testGetDashMpd() throws ParsingException {
            // we dont expect this particular video to have a DASH file. For this purpouse we use a different test class.
            assertTrue(extractor.getDashMpdUrl(),
                    extractor.getDashMpdUrl() != null && extractor.getDashMpdUrl().isEmpty());
        }

        @Test
        public void testGetRelatedVideos() throws ExtractionException {
            StreamInfoItemsCollector relatedVideos = extractor.getRelatedStreams();
            Utils.printErrors(relatedVideos.getErrors());
            assertFalse(relatedVideos.getItems().isEmpty());
            assertTrue(relatedVideos.getErrors().isEmpty());
        }

        @Test
        public void testGetSubtitlesListDefault() {
            // Video (/view?v=YQHsXMglC9A) set in the setUp() method has no captions => null
            assertTrue(extractor.getSubtitlesDefault().isEmpty());
        }

        @Test
        public void testGetSubtitlesList() {
            // Video (/view?v=YQHsXMglC9A) set in the setUp() method has no captions => null
            assertTrue(extractor.getSubtitles(MediaFormat.TTML).isEmpty());
        }

        @Test
        public void testGetLikeCount() throws ParsingException {
            long likeCount = extractor.getLikeCount();
            assertTrue("" + likeCount, likeCount >= 15000000);
        }

        @Test
        public void testGetDislikeCount() throws ParsingException {
            long dislikeCount = extractor.getDislikeCount();
            assertTrue("" + dislikeCount, dislikeCount >= 818000);
        }
    }

    public static class DescriptionTestPewdiepie {
        private static YoutubeStreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeStreamExtractor) YouTube
                    .getStreamExtractor("https://www.youtube.com/watch?v=fBc4Q_htqPg");
            extractor.fetchPage();
        }

        @Test
        public void testGetDescription() throws ParsingException {
            assertNotNull(extractor.getDescription());
            assertFalse(extractor.getDescription().getContent().isEmpty());
        }

        @Test
        public void testGetFullLinksInDescription() throws ParsingException {
            assertTrue(extractor.getDescription().getContent().contains("https://www.reddit.com/r/PewdiepieSubmissions/"));
            assertTrue(extractor.getDescription().getContent().contains("https://www.youtube.com/channel/UC3e8EMTOn4g6ZSKggHTnNng"));
            assertTrue(extractor.getDescription().getContent().contains("https://usa.clutchchairz.com/product/pewdiepie-edition-throttle-series/"));
        }
    }

    public static class DescriptionTestUnboxing {
        private static YoutubeStreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeStreamExtractor) YouTube
                    .getStreamExtractor("https://www.youtube.com/watch?v=cV5TjZCJkuA");
            extractor.fetchPage();
        }

        @Test
        public void testGetDescription() throws ParsingException {
            assertNotNull(extractor.getDescription());
            assertFalse(extractor.getDescription().getContent().isEmpty());
        }

        @Test
        public void testGetFullLinksInDescription() throws ParsingException {
            final String description = extractor.getDescription().getContent();
            assertTrue(description.contains("https://www.youtube.com/watch?v=X7FLCHVXpsA&amp;list=PL7u4lWXQ3wfI_7PgX0C-VTiwLeu0S4v34"));
            assertTrue(description.contains("https://www.youtube.com/watch?v=Lqv6G0pDNnw&amp;list=PL7u4lWXQ3wfI_7PgX0C-VTiwLeu0S4v34"));
            assertTrue(description.contains("https://www.youtube.com/watch?v=XxaRBPyrnBU&amp;list=PL7u4lWXQ3wfI_7PgX0C-VTiwLeu0S4v34"));
            assertTrue(description.contains("https://www.youtube.com/watch?v=U-9tUEOFKNU&amp;list=PL7u4lWXQ3wfI_7PgX0C-VTiwLeu0S4v34"));
        }
    }

    public static class RatingsDisabledTest {
        private static YoutubeStreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeStreamExtractor) YouTube
                    .getStreamExtractor("https://www.youtube.com/watch?v=HRKu0cvrr_o");
            extractor.fetchPage();
        }

        @Test
        public void testGetLikeCount() throws ParsingException {
            assertEquals(-1, extractor.getLikeCount());
        }

        @Test
        public void testGetDislikeCount() throws ParsingException {
            assertEquals(-1, extractor.getDislikeCount());
        }

    }

    public static class FramesTest {
        private static YoutubeStreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeStreamExtractor) YouTube
                    .getStreamExtractor("https://www.youtube.com/watch?v=HoK9shIJ2xQ");
            extractor.fetchPage();
        }

        @Test
        public void testGetFrames() throws ExtractionException {
            final List<Frameset> frames = extractor.getFrames();
            assertNotNull(frames);
            assertFalse(frames.isEmpty());
            for (final Frameset f : frames) {
                for (final String url : f.getUrls()) {
                    ExtractorAsserts.assertIsValidUrl(url);
                    ExtractorAsserts.assertIsSecureUrl(url);
                }
            }
        }
    }
}
