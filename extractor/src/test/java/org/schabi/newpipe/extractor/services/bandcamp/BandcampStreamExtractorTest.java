// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampStreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

public class BandcampStreamExtractorTest {

    private static BandcampStreamExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (BandcampStreamExtractor) Bandcamp
                .getStreamExtractor("https://zachbenson.bandcamp.com/track/kitchen");
        extractor.fetchPage();
    }

    @Test
    public void testServiceId() {
        assertEquals(4, extractor.getServiceId());
    }

    @Test
    public void testName() throws ParsingException {
        assertEquals("kitchen", extractor.getName());
    }

    @Test
    public void testUrl() throws ParsingException {
        assertEquals("https://zachbenson.bandcamp.com/track/kitchen", extractor.getUrl());
    }

    @Test
    public void testArtistUrl() throws ParsingException {
        assertEquals("https://zachbenson.bandcamp.com/", extractor.getUploaderUrl());
    }

    @Test
    public void testDescription() {
        assertTrue(extractor.getDescription().getContent().contains("Boy, you've taken up my thoughts"));
    }

    @Test
    public void testArtistProfilePicture() {
        String url = extractor.getUploaderAvatarUrl();
        assertTrue(url.contains("://f4.bcbits.com/img/") && url.endsWith(".jpg"));
    }

    @Test
    public void testUploadDate() throws ParsingException {
        final Calendar expectedCalendar = Calendar.getInstance();
        // 27 Sep 2019 21:49:14 GMT
        expectedCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        expectedCalendar.set(2019, Calendar.SEPTEMBER, 27, 21, 49, 14);
        expectedCalendar.set(Calendar.MILLISECOND, 0);

        assertEquals(expectedCalendar.getTimeInMillis(), extractor.getUploadDate().date().getTimeInMillis());
    }

    @Test
    public void testNoArtistProfilePicture() throws ExtractionException {
        assertEquals("", Bandcamp.getStreamExtractor("https://powertothequeerkids.bandcamp.com/track/human-nature").getUploaderAvatarUrl());
    }

    @Test
    public void testAudioStream() {
        assertTrue(extractor.getAudioStreams().get(0).getUrl().contains("bcbits.com/stream"));
        assertEquals(1, extractor.getAudioStreams().size());
    }

    @Test
    public void testCategory() throws ExtractionException, IOException {
        StreamExtractor se = Bandcamp.getStreamExtractor("https://npet.bandcamp.com/track/track-1");
        se.fetchPage();
        assertEquals("acoustic", se.getCategory());
    }

    @Test
    public void testLicense() throws ExtractionException, IOException {
        StreamExtractor se = Bandcamp.getStreamExtractor("https://npet.bandcamp.com/track/track-1");
        se.fetchPage();
        assertEquals("CC BY 3.0", se.getLicence());
    }

    @Test
    public void testTranslateIdsToUrl() throws ParsingException {
        assertEquals("https://zachbenson.bandcamp.com/album/covers", BandcampExtractorHelper.getStreamUrlFromIds(2862267535L, 2063639444L, "album"));
        // TODO write more test cases
    }

}
