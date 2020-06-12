package org.schabi.newpipe.extractor.services.youtube.invidious;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.invidious.extractors.InvidiousStreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.Invidious;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * InvidiousStreamExtractorTest.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */

public class InvidiousStreamExtractorTest {

    /**
     * Test for {@link StreamExtractor}
     */
    public static class AdeleHello {
        private static InvidiousStreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (InvidiousStreamExtractor) Invidious
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
            StreamExtractor extractor = Invidious.getStreamExtractor("https://youtu.be/FmG385_uUys?t=174");
            assertEquals(extractor.getTimeStamp() + "", "174");
        }

        @Test
        public void testGetTitle() throws ParsingException {
            assertFalse(extractor.getName().isEmpty());
        }

        @Test
        public void testGetDescription() {
            assertNotNull(extractor.getDescription());
            assertFalse(extractor.getDescription().getContent().isEmpty());
        }

        @Test
        public void testGetFullLinksInDescription() {
            assertTrue(extractor.getDescription().getContent().contains("http://adele.com"));
        }

        @Test
        public void testGetUploaderName() {
            assertNotNull(extractor.getUploaderName());
            assertFalse(extractor.getUploaderName().isEmpty());
        }

        @Test
        public void testGetLength() {
            assertEquals(367, extractor.getLength());
        }

        @Test
        public void testGetViewCount() {
            final long count = extractor.getViewCount();
            assertTrue(Long.toString(count), count >= /* specific to that video */ 1220025784);
        }

        @Test
        public void testGetUploadDate() throws ParseException {
            final Calendar instance = Calendar.getInstance();
            instance.setTime(new SimpleDateFormat("yyyy-MM-dd").parse("2015-10-22"));
            assertEquals(instance, requireNonNull(extractor.getUploadDate()).date());
        }

        @Test
        public void testGetUploaderUrl() throws ParsingException {
            final String url = extractor.getUploaderUrl();
            if (!url.endsWith("channel/UCsRM0YB_dabtEPGPTKo-gcw") &&
                    !url.endsWith("channel/UComP_epzeKzvBX156r6pm1Q")) {
                fail("Uploader url is neither the music channel one nor the Vevo one");
            }
        }

        @Test
        public void testGetThumbnailUrl() {
            assertIsSecureUrl(extractor.getThumbnailUrl());
        }

        @Test
        public void testGetUploaderAvatarUrl() {
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
        public void testStreamType() {
            assertSame(extractor.getStreamType(), StreamType.VIDEO_STREAM);
        }

        @Test
        public void testGetDashMpd() {
            assertTrue(extractor.getDashMpdUrl().endsWith("api/manifest/dash/id/YQHsXMglC9A"));
        }

        @Ignore
        @Test
        public void testGetRelatedVideos() throws ExtractionException {
            final StreamInfoItemsCollector relatedVideos = extractor.getRelatedStreams();
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
        public void testPrivacy() {
            assertEquals("Public", extractor.getPrivacy());
        }

        @Test
        public void testGetLikeCount() {
            long likeCount = extractor.getLikeCount();
            assertTrue("" + likeCount, likeCount >= 15000000);
        }

        @Test
        public void testGetDislikeCount() {
            long dislikeCount = extractor.getDislikeCount();
            assertTrue("" + dislikeCount, dislikeCount >= 818000);
        }
    }

    public static class Subtitles {

        private static InvidiousStreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (InvidiousStreamExtractor) Invidious
                    .getStreamExtractor("http://invidio.us/watch?v=wu8sEoneUaA");
            extractor.fetchPage();
        }

        @Test
        public void testGetSubtitles() {
            final List<SubtitlesStream> subtitles = extractor.getSubtitlesDefault();
            final SubtitlesStream subtitle = subtitles.get(0);
            assertIsSecureUrl(subtitle.getUrl());
        }

    }
}
