package org.schabi.newpipe.extractor.services.bandcamp.stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.services.DefaultTests;
import org.schabi.newpipe.extractor.services.bandcamp.BandcampTestUtils;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampRadioStreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

public class BandcampRadioStreamExtractorTest extends DefaultStreamExtractorTest {
    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/bandcamp/extractor/stream/radio";

    private static StreamExtractor extractor;

    private static final String URL = "https://bandcamp.com/?show=230";

    @BeforeAll
    public static void setUp() throws IOException, ExtractionException {
        NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH));
        extractor = Bandcamp.getStreamExtractor(URL);
        extractor.fetchPage();
    }

    @Test
    void testGettingCorrectStreamExtractor() throws ExtractionException {
        assertTrue(Bandcamp.getStreamExtractor("https://bandcamp.com/?show=3") instanceof BandcampRadioStreamExtractor);
        assertFalse(Bandcamp.getStreamExtractor("https://zachbenson.bandcamp.com/track/deflated")
                instanceof BandcampRadioStreamExtractor);
    }

    @Override public StreamExtractor extractor() { return extractor; }
    @Override public String expectedName() throws Exception { return "Sound Movements"; }
    @Override public String expectedId() throws Exception { return "230"; }
    @Override public String expectedUrlContains() throws Exception { return URL; }
    @Override public String expectedOriginalUrlContains() throws Exception { return URL; }
    @Override public boolean expectedHasVideoStreams() { return false; }
    @Override public boolean expectedHasSubtitles() { return false; }
    @Override public boolean expectedHasFrames() { return false; }
    @Override public boolean expectedHasRelatedItems() { return false; }
    @Override public StreamType expectedStreamType() { return StreamType.AUDIO_STREAM; }
    @Override public StreamingService expectedService() { return Bandcamp; }
    @Override public String expectedUploaderName() { return "Andrew Jervis"; }
    @Override public int expectedStreamSegmentsCount() { return 30; }

    @Test
    void testGetUploaderUrl() {
        assertThrows(ContentNotSupportedException.class, extractor::getUploaderUrl);
    }

    @Test
    @Override
    public void testUploaderUrl() {
        assertThrows(ContentNotSupportedException.class, super::testUploaderUrl);
    }

    @Override public String expectedUploaderUrl() { return null; }

    @Override
    public List<String> expectedDescriptionContains() {
        return Collections.singletonList("Featuring special guests Nick Hakim and Elbows, plus fresh cuts from Eddie Palmieri, KRS One, Ladi6, and Moonchild.");
    }

    @Override public long expectedLength() { return 5619; }
    @Override public long expectedViewCountAtLeast() { return -1; }
    @Override public long expectedLikeCountAtLeast() { return -1; }
    @Override public long expectedDislikeCountAtLeast() { return -1; }

    @Override public String expectedUploadDate() { return "16 May 2017 00:00:00 GMT"; }
    @Override public String expectedTextualUploadDate() { return "16 May 2017 00:00:00 GMT"; }
    @Test
    public void testUploadDate() throws ParsingException {
        final Calendar expectedCalendar = Calendar.getInstance();

        // 16 May 2017 00:00:00 GMT
        expectedCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        expectedCalendar.setTimeInMillis(0);
        expectedCalendar.set(2017, Calendar.MAY, 16);

        assertEquals(expectedCalendar.getTimeInMillis(), extractor.getUploadDate().offsetDateTime().toInstant().toEpochMilli());
    }

    @Test
    void testGetThumbnails() throws ParsingException {
        BandcampTestUtils.testImages(extractor.getThumbnails());
    }

    @Test
    void testGetUploaderAvatars() throws ParsingException {
        DefaultTests.defaultTestImageCollection(extractor.getUploaderAvatars());
        extractor.getUploaderAvatars().forEach(image ->
                ExtractorAsserts.assertContains("bandcamp-button", image.getUrl()));
    }

    @Test
    void testGetAudioStreams() throws ExtractionException, IOException {
        assertEquals(1, extractor.getAudioStreams().size());
    }
}
