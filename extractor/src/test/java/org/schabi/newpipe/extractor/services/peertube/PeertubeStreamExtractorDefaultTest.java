package org.schabi.newpipe.extractor.services.peertube;

import org.junit.BeforeClass;
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

import java.io.IOException;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;

/**
 * Test for {@link StreamExtractor}
 */
public class PeertubeStreamExtractorDefaultTest {
    private static PeertubeStreamExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance(), new Localization("GB", "en"));
        extractor = (PeertubeStreamExtractor) PeerTube.getStreamExtractor("https://peertube.mastodon.host/videos/watch/86fe4f24-64c3-4ab4-9e7e-66177219ed21");
        extractor.fetchPage();
    }

    @Test
    public void testGetInvalidTimeStamp() throws ParsingException {
        assertTrue(extractor.getTimeStamp() + "",
                extractor.getTimeStamp() <= 0);
    }

    @Test
    public void testGetTitle() throws ParsingException {
        assertEquals(extractor.getName(), "Pažadėtoji 1 Sezonas 1050 Serija - Ziuri.me");
    }

    @Test
    public void testGetDescription() throws ParsingException {
        assertEquals(extractor.getDescription(), "Serialo veiksmas vyksta Radžastane. kur vis dar gyvos liaudies tradicijos. o jo centre - Anandi gimusi varguolių šeimoje. Mergaitė privalės ištekėti už turtingo paveldėtojo vos tik sulaukusi aštuonerių metų ir priprasti prie naujojo nuotakos vaidm...");
    }

    @Test
    public void testGetUploaderName() throws ParsingException {
        assertEquals(extractor.getUploaderName(), "djsets");
    }

    @Test
    public void testGetLength() throws ParsingException {
        assertEquals(extractor.getLength(), 1238);
    }

    @Test
    public void testGetViewCount() throws ParsingException {
        assertTrue(Long.toString(extractor.getViewCount()),
                extractor.getViewCount() > 700);
    }

    @Test
    public void testGetUploadDate() throws ParsingException {
        assertEquals("2018-10-06", extractor.getUploadDate());
    }

    @Test
    public void testGetUploaderUrl() throws ParsingException {
        assertIsSecureUrl(extractor.getUploaderUrl());
        assertEquals("https://peertube.mastodon.host/api/v1/accounts/djsets@hostyour.tv", extractor.getUploaderUrl());
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

    @Test
    public void testGetRelatedVideos() throws ExtractionException, IOException {
        StreamInfoItemsCollector relatedVideos = extractor.getRelatedVideos();
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
