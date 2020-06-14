package org.schabi.newpipe.extractor.services.peertube;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeStreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;

/**
 * Test for {@link StreamExtractor}
 */
public class PeertubeStreamExtractorDefaultTest {
    private static PeertubeStreamExtractor extractor;
    private static final String expectedLargeDescription = "**[Want to help to translate this video?](https://weblate.framasoft.org/projects/what-is-peertube-video/)**\r\n\r\n**Take back the control of your videos! [#JoinPeertube](https://joinpeertube.org)**\r\n*A decentralized video hosting network, based on free/libre software!*\r\n\r\n**Animation Produced by:** [LILA](https://libreart.info) - [ZeMarmot Team](https://film.zemarmot.net)\r\n*Directed by* Aryeom\r\n*Assistant* Jehan\r\n**Licence**: [CC-By-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/)\r\n\r\n**Sponsored by** [Framasoft](https://framasoft.org)\r\n\r\n**Music**: [Red Step Forward](http://play.dogmazic.net/song.php?song_id=52491) - CC-By Ken Bushima\r\n\r\n**Movie Clip**: [Caminades 3: Llamigos](http://www.caminandes.com/) CC-By Blender Institute\r\n\r\n**Video sources**: https://gitlab.gnome.org/Jehan/what-is-peertube/";
    private static final String expectedSmallDescription = "https://www.kickstarter.com/projects/1587081065/nothing-to-hide-the-documentary";

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        // setting instance might break test when running in parallel
        PeerTube.setInstance(new PeertubeInstance("https://framatube.org", "FramaTube"));
        extractor = (PeertubeStreamExtractor) PeerTube.getStreamExtractor("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d");
        extractor.fetchPage();
    }

    @Test
    public void testGetUploadDate() throws ParsingException, ParseException {
        final Calendar instance = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        instance.setTime(sdf.parse("2018-10-01T10:52:46.396Z"));
        assertEquals(instance, requireNonNull(extractor.getUploadDate()).date());

    }

    @Test
    public void testGetInvalidTimeStamp() throws ParsingException {
        assertTrue(extractor.getTimeStamp() + "",
                extractor.getTimeStamp() <= 0);
    }

    @Test
    public void testGetTitle() throws ParsingException {
        assertEquals("What is PeerTube?", extractor.getName());
    }

    @Test
    public void testGetLargeDescription() throws ParsingException {
        assertEquals(expectedLargeDescription, extractor.getDescription().getContent());
    }

    @Test
    public void testGetEmptyDescription() throws Exception {
        PeertubeStreamExtractor extractorEmpty = (PeertubeStreamExtractor) PeerTube.getStreamExtractor("https://framatube.org/api/v1/videos/d5907aad-2252-4207-89ec-a4b687b9337d");
        extractorEmpty.fetchPage();
        assertEquals("", extractorEmpty.getDescription().getContent());
    }

    @Test
    public void testGetSmallDescription() throws Exception {
        PeerTube.setInstance(new PeertubeInstance("https://peertube.cpy.re", "PeerTube test server"));
        PeertubeStreamExtractor extractorSmall = (PeertubeStreamExtractor) PeerTube.getStreamExtractor("https://peertube.cpy.re/videos/watch/d2a5ec78-5f85-4090-8ec5-dc1102e022ea");
        extractorSmall.fetchPage();
        assertEquals(expectedSmallDescription, extractorSmall.getDescription().getContent());
    }

    @Test
    public void testGetUploaderName() throws ParsingException {
        assertEquals("Framasoft", extractor.getUploaderName());
    }

    @Test
    public void testGetUploaderUrl() throws ParsingException {
        assertIsSecureUrl(extractor.getUploaderUrl());
        assertEquals("https://framatube.org/api/v1/accounts/framasoft@framatube.org", extractor.getUploaderUrl());
    }

    @Test
    public void testGetUploaderAvatarUrl() throws ParsingException {
        assertIsSecureUrl(extractor.getUploaderAvatarUrl());
    }

    @Test
    public void testGetSubChannelName() throws ParsingException {
        assertEquals("Les vidéos de Framasoft", extractor.getSubChannelName());
    }

    @Test
    public void testGetSubChannelUrl() throws ParsingException {
        assertIsSecureUrl(extractor.getSubChannelUrl());
        assertEquals("https://framatube.org/video-channels/bf54d359-cfad-4935-9d45-9d6be93f63e8", extractor.getSubChannelUrl());
    }

    @Test
    public void testGetSubChannelAvatarUrl() throws ParsingException {
        assertIsSecureUrl(extractor.getSubChannelAvatarUrl());
    }

    @Test
    public void testGetLength() throws ParsingException {
        assertEquals(113, extractor.getLength());
    }

    @Test
    public void testGetViewCount() throws ParsingException {
        assertTrue(Long.toString(extractor.getViewCount()),
                extractor.getViewCount() > 10);
    }

    @Test
    public void testGetThumbnailUrl() throws ParsingException {
        assertIsSecureUrl(extractor.getThumbnailUrl());
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
        assertFalse(extractor.getSubtitlesDefault().isEmpty());
    }

    @Test
    public void testGetSubtitlesList() throws IOException, ExtractionException {
        assertFalse(extractor.getSubtitlesDefault().isEmpty());
    }

    @Test
    public void testGetAgeLimit() throws ExtractionException, IOException {
        assertEquals(0, extractor.getAgeLimit());
        PeertubeStreamExtractor ageLimit = (PeertubeStreamExtractor) PeerTube.getStreamExtractor("https://nocensoring.net/videos/embed/dbd8e5e1-c527-49b6-b70c-89101dbb9c08");
        ageLimit.fetchPage();
        assertEquals(18, ageLimit.getAgeLimit());
    }

    @Test
    public void testGetSupportInformation() throws ExtractionException, IOException {
        PeertubeStreamExtractor supportInfoExtractor = (PeertubeStreamExtractor) PeerTube.getStreamExtractor("https://framatube.org/videos/watch/ee408ec8-07cd-4e35-b884-fb681a4b9d37");
        supportInfoExtractor.fetchPage();
        assertEquals("https://utip.io/chatsceptique", supportInfoExtractor.getSupportInfo());
    }

    @Test
    public void testGetLanguageInformation() throws ParsingException {
        assertEquals(new Locale("en"), extractor.getLanguageInfo());
    }
}
