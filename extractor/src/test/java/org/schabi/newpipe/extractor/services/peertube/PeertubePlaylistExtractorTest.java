package org.schabi.newpipe.extractor.services.peertube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestImageCollection;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubePlaylistExtractor;

public class PeertubePlaylistExtractorTest {

    public static class Shocking {
        private static PeertubePlaylistExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (PeertubePlaylistExtractor) PeerTube.getPlaylistExtractor(
                    "https://framatube.org/videos/watch/playlist/96b0ee2b-a5a7-4794-8769-58d8ccb79ab7");
            extractor.fetchPage();
        }

        @Test
        void testGetName() throws ParsingException {
            assertEquals("Shocking !", extractor.getName());
        }

        @Test
        void testGetThumbnails() throws ParsingException {
            defaultTestImageCollection(extractor.getThumbnails());
        }

        @Test
        void testGetUploaderUrl() {
            assertEquals("https://skeptikon.fr/accounts/metadechoc", extractor.getUploaderUrl());
        }

        @Test
        void testGetUploaderAvatars() throws ParsingException {
            defaultTestImageCollection(extractor.getUploaderAvatars());
        }

        @Test
        void testGetUploaderName() {
            assertEquals("Méta de Choc", extractor.getUploaderName());
        }

        @Test
        void testGetStreamCount() {
            ExtractorAsserts.assertGreaterOrEqual(39, extractor.getStreamCount());
        }

        @Test
        void testGetDescription() throws ParsingException {
            ExtractorAsserts.assertContains("épisodes de Shocking", extractor.getDescription().getContent());
        }

        @Test
        void testGetSubChannelUrl() {
            assertEquals("https://skeptikon.fr/video-channels/metadechoc_channel", extractor.getSubChannelUrl());
        }

        @Test
        void testGetSubChannelName() {
            assertEquals("SHOCKING !", extractor.getSubChannelName());
        }

        @Test
        void testGetSubChannelAvatars() throws ParsingException {
            defaultTestImageCollection(extractor.getSubChannelAvatars());
        }
    }
}
