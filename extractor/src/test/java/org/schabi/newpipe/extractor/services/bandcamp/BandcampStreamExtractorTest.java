// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampStreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

/**
 * Tests for {@link BandcampStreamExtractor}
 */
public class BandcampStreamExtractorTest extends DefaultStreamExtractorTest {

    private static BandcampStreamExtractor extractor;

    @BeforeClass
    public static void setUp() throws ExtractionException, IOException {
        NewPipe.init(DownloaderTestImpl.getInstance());
        // This test track was uploaded specifically for NewPipeExtractor tests

        extractor = (BandcampStreamExtractor) Bandcamp
                .getStreamExtractor("https://npet.bandcamp.com/track/track-1");
        extractor.fetchPage();
    }

    @Override
    public StreamExtractor extractor() {
        return extractor;
    }

    @Override
    public StreamingService expectedService() {
        return Bandcamp;
    }

    @Override
    public String expectedName() {
        return "Track #1";
    }

    @Override
    public String expectedId() {
        return "https://npet.bandcamp.com/track/track-1";
    }

    @Override
    public String expectedUrlContains() {
        return "https://npet.bandcamp.com/track/track-1";
    }

    @Override
    public String expectedOriginalUrlContains() {
        return "https://npet.bandcamp.com/track/track-1";
    }

    @Override
    public StreamType expectedStreamType() {
        return StreamType.AUDIO_STREAM;
    }

    @Override
    public String expectedUploaderName() {
        return "NewPipeExtractorTest";
    }

    @Override
    public String expectedUploaderUrl() {
        return "https://npet.bandcamp.com/";
    }

    @Override
    public List<String> expectedDescriptionContains() {
        return Collections.singletonList("This sample track was created using MuseScore.");
    }

    @Override
    public long expectedLength() {
        return 0;
    }

    @Override
    public long expectedViewCountAtLeast() {
        return Long.MIN_VALUE;
    }

    @Override
    public String expectedUploadDate() {
        return "2020-03-17 18:37:44.000";
    }

    @Override
    public String expectedTextualUploadDate() {
        return "17 Mar 2020 18:37:44 GMT";
    }

    @Override
    public long expectedLikeCountAtLeast() {
        return Long.MIN_VALUE;
    }

    @Override
    public long expectedDislikeCountAtLeast() {
        return Long.MIN_VALUE;
    }

    @Override
    public boolean expectedHasVideoStreams() {
        return false;
    }

    @Override
    public boolean expectedHasRelatedStreams() {
        return false;
    }

    @Override
    public boolean expectedHasSubtitles() {
        return false;
    }

    @Override
    public boolean expectedHasFrames() {
        return false;
    }

    @Override
    public String expectedLicence() {
        return "CC BY 3.0";
    }

    @Override
    public String expectedCategory() {
        return "acoustic";
    }

    @Test
    public void testArtistProfilePicture() throws Exception {
        String url = extractor().getUploaderAvatarUrl();
        assertTrue(url.contains("://f4.bcbits.com/img/") && url.endsWith(".jpg"));
    }

    @Test
    public void testTranslateIdsToUrl() throws ParsingException {
        assertEquals("https://npet.bandcamp.com/track/track-1", BandcampExtractorHelper.getStreamUrlFromIds(3775652329L, 4207805220L, "track"));
    }

}
