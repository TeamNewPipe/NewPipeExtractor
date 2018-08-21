package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.stream.*;
import org.schabi.newpipe.extractor.utils.DashMpdParser;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;

import static org.junit.Assert.*;
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
    private static YoutubeStreamExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        extractor = (YoutubeStreamExtractor) YouTube
                .getStreamExtractor("https://www.youtube.com/watch?v=rYEDA3JcQqw");
        extractor.fetchPage();
    }

    @Test
    public void testGetInvalidTimeStamp() throws ParsingException {
        assertTrue(extractor.getTimeStamp() + "",
                extractor.getTimeStamp() <= 0);
    }

    @Test
    public void testGetValidTimeStamp() throws IOException, ExtractionException {
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
        assertFalse(extractor.getDescription().isEmpty());
    }

    @Test
    public void testGetFullLinksInDescriptlion() throws ParsingException {
        assertTrue(extractor.getDescription().contains("http://smarturl.it/SubscribeAdele?IQid=yt"));
        assertFalse(extractor.getDescription().contains("http://smarturl.it/SubscribeAdele?IQi..."));
        System.out.println(extractor.getDescription());
    }

    @Test
    public void testGetUploaderName() throws ParsingException {
        assertNotNull(extractor.getUploaderName());
        assertFalse(extractor.getUploaderName().isEmpty());
    }


    @Test
    public void testGetLength() throws ParsingException {
        assertTrue(extractor.getLength() > 0);
    }

    @Test
    public void testGetViewCount() throws ParsingException {
        Long count = extractor.getViewCount();
        assertTrue(Long.toString(count), count >= /* specific to that video */ 1220025784);
    }

    @Test
    public void testGetUploadDate() throws ParsingException {
        assertTrue(extractor.getUploadDate().length() > 0);
    }

    @Test
    public void testGetUploaderUrl() throws ParsingException {
        assertTrue(extractor.getUploaderUrl().length() > 0);
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
    public void testGetVideoStreams() throws IOException, ExtractionException {
        for (VideoStream s : extractor.getVideoStreams()) {
            assertIsSecureUrl(s.url);
            assertTrue(s.resolution.length() > 0);
            assertTrue(Integer.toString(s.getFormatId()),
                    0 <= s.getFormatId() && s.getFormatId() <= 4);
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
    public void testGetRelatedVideos() throws ExtractionException, IOException {
        StreamInfoItemsCollector relatedVideos = extractor.getRelatedVideos();
        Utils.printErrors(relatedVideos.getErrors());
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
        assertTrue(extractor.getSubtitles(SubtitlesFormat.TTML).isEmpty());
    }
}
