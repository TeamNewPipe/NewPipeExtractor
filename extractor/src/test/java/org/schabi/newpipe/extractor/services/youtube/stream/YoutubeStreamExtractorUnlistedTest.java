package org.schabi.newpipe.extractor.services.youtube.stream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class YoutubeStreamExtractorUnlistedTest {
    private static YoutubeStreamExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (YoutubeStreamExtractor) YouTube
                .getStreamExtractor("https://www.youtube.com/watch?v=udsB8KnIJTg");
        extractor.fetchPage();
    }

    @Test
    public void testGetInvalidTimeStamp() throws ParsingException {
        assertTrue(extractor.getTimeStamp() + "",
                extractor.getTimeStamp() <= 0);
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
        assertTrue(extractor.getDescription().getContent().contains("https://www.youtube.com/user/Roccowschiptune"));
    }

    @Test
    public void testGetUploaderName() throws ParsingException {
        assertNotNull(extractor.getUploaderName());
        assertFalse(extractor.getUploaderName().isEmpty());
    }


    @Test
    public void testGetLength() throws ParsingException {
        assertEquals(2488, extractor.getLength());
    }

    @Test
    public void testGetViewCount() throws ParsingException {
        long count = extractor.getViewCount();
        assertTrue(Long.toString(count), count >= /* specific to that video */ 1225);
    }

    @Test
    public void testGetTextualUploadDate() throws ParsingException {
        Assert.assertEquals("2017-09-22", extractor.getTextualUploadDate());
    }

    @Test
    public void testGetUploadDate() throws ParsingException, ParseException {
        final Calendar instance = Calendar.getInstance();
        instance.setTime(new SimpleDateFormat("yyyy-MM-dd").parse("2017-09-22"));
        assertNotNull(extractor.getUploadDate());
        assertEquals(instance, extractor.getUploadDate().date());
    }

    @Test
    public void testGetUploaderUrl() throws ParsingException {
        assertEquals("https://www.youtube.com/channel/UCPysfiuOv4VKBeXFFPhKXyw", extractor.getUploaderUrl());
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
        List<AudioStream> audioStreams = extractor.getAudioStreams();
        assertFalse(audioStreams.isEmpty());
        for (AudioStream s : audioStreams) {
            assertIsSecureUrl(s.url);
            assertTrue(Integer.toString(s.getFormatId()),
                    0x100 <= s.getFormatId() && s.getFormatId() < 0x1000);
        }
    }

    @Test
    public void testGetVideoStreams() throws ExtractionException {
        for (VideoStream s : extractor.getVideoStreams()) {
            assertIsSecureUrl(s.url);
            assertTrue(s.resolution.length() > 0);
            assertTrue(Integer.toString(s.getFormatId()),
                    0 <= s.getFormatId() && s.getFormatId() < 0x100);
        }
    }

    @Test
    public void testStreamType() throws ParsingException {
        assertSame(StreamType.VIDEO_STREAM, extractor.getStreamType());
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
        assertFalse(extractor.getSubtitlesDefault().isEmpty());
    }

    @Test
    public void testGetSubtitlesList() {
        assertFalse(extractor.getSubtitles(MediaFormat.TTML).isEmpty());
    }

    @Test
    public void testGetLikeCount() throws ParsingException {
        long likeCount = extractor.getLikeCount();
        assertTrue("" + likeCount, likeCount >= 96);
    }

    @Test
    public void testGetDislikeCount() throws ParsingException {
        long dislikeCount = extractor.getDislikeCount();
        assertTrue("" + dislikeCount, dislikeCount >= 0);
    }
}
