package org.schabi.newpipe.extractor.services.peertube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeStreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.Localization;

/**
 * Test for {@link StreamExtractor}
 */
public class PeertubeStreamExtractorDefaultTest {
    private static PeertubeStreamExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance(), new Localization("GB", "en"));
        // setting instance might break test when running in parallel
        PeerTube.setInstance("https://peertube.mastodon.host", "PeerTube on Mastodon.host");
        extractor = (PeertubeStreamExtractor) PeerTube.getStreamExtractor("https://peertube.mastodon.host/videos/watch/afe5bf12-c58b-4efd-b56e-29c5a59e04bc");
        extractor.fetchPage();
    }

    @Test
    public void testGetInvalidTimeStamp() throws ParsingException {
        assertTrue(extractor.getTimeStamp() + "",
                extractor.getTimeStamp() <= 0);
    }

    @Test
    public void testGetTitle() throws ParsingException {
        assertEquals(extractor.getName(), "Power Corrupts the Best");
    }

    @Test
    public void testGetDescription() throws ParsingException {
        assertEquals(extractor.getDescription(), "A short reading from Bakunin, made for the group Audible Anarchist https://audibleanarchist.github.io/Webpage/");
    }

    @Test
    public void testGetUploaderName() throws ParsingException {
        assertEquals(extractor.getUploaderName(), "Rowsedower");
    }

    @Test
    public void testGetLength() throws ParsingException {
        assertEquals(extractor.getLength(), 269);
    }

    @Test
    public void testGetViewCount() throws ParsingException {
        assertTrue(Long.toString(extractor.getViewCount()),
                extractor.getViewCount() > 10);
    }

    @Test
    public void testGetUploadDate() throws ParsingException {
        assertEquals("2018-09-30", extractor.getUploadDate());
    }

    @Test
    public void testGetUploaderUrl() throws ParsingException {
        assertIsSecureUrl(extractor.getUploaderUrl());
        assertEquals("https://peertube.mastodon.host/api/v1/accounts/reddebrek@peertube.mastodon.host", extractor.getUploaderUrl());
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
    public void testGetVideoStreams() throws IOException, ExtractionException {
        assertFalse(extractor.getVideoStreams().isEmpty());
    }

    @Test
    public void testStreamType() throws ParsingException {
        assertTrue(extractor.getStreamType() == StreamType.VIDEO_STREAM);
    }

    @Ignore
    @Test
    public void testGetRelatedVideos() throws ExtractionException, IOException {
        StreamInfoItemsCollector relatedVideos = extractor.getRelatedStreams();
        assertFalse(relatedVideos.getItems().isEmpty());
        assertTrue(relatedVideos.getErrors().isEmpty());
    }

    @Test
    public void testGetSubtitlesListDefault() throws IOException, ExtractionException {
        assertTrue(extractor.getSubtitlesDefault().isEmpty());
    }

    @Test
    public void testGetSubtitlesList() throws IOException, ExtractionException {
        assertTrue(extractor.getSubtitlesDefault().isEmpty());
    }
}
