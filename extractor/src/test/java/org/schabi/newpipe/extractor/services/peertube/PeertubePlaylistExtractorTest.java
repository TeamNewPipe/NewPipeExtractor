package org.schabi.newpipe.extractor.services.peertube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubePlaylistExtractor;

import static org.junit.Assert.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;

public class PeertubePlaylistExtractorTest {

    public static class Shocking {
        private static PeertubePlaylistExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (PeertubePlaylistExtractor) PeerTube
                    .getPlaylistExtractor("https://framatube.org/videos/watch/playlist/96b0ee2b-a5a7-4794-8769-58d8ccb79ab7");
            extractor.fetchPage();
        }

        @Test
        public void testGetName() throws ParsingException {
            assertEquals("Shocking !", extractor.getName());
        }

        @Test
        public void testGetThumbnailUrl() throws ParsingException {
            assertEquals("https://framatube.org/static/thumbnails/playlist-96b0ee2b-a5a7-4794-8769-58d8ccb79ab7.jpg", extractor.getThumbnailUrl());
        }

        @Test
        public void testGetUploaderUrl() throws ParsingException {
            assertEquals("https://skeptikon.fr/accounts/metadechoc", extractor.getUploaderUrl());
        }

        @Test
        public void testGetUploaderAvatarUrl() throws ParsingException {
            assertEquals("https://framatube.org/lazy-static/avatars/cd0f781d-0287-4be2-94f1-24cd732337b2.jpg", extractor.getUploaderAvatarUrl());
        }

        @Test
        public void testGetUploaderName() throws ParsingException {
            assertEquals("Méta de Choc", extractor.getUploaderName());
        }

        @Test
        public void testGetStreamCount() throws ParsingException {
            assertEquals(35, extractor.getStreamCount());
        }

        @Test
        public void testGetSubChannelUrl() throws ParsingException {
            assertEquals("https://skeptikon.fr/video-channels/metadechoc_channel", extractor.getSubChannelUrl());
        }

        @Test
        public void testGetSubChannelName() throws ParsingException {
            assertEquals("SHOCKING !", extractor.getSubChannelName());
        }

        @Test
        public void testGetSubChannelAvatarUrl() throws ParsingException {
            assertEquals("https://framatube.org/lazy-static/avatars/f1dcd0e8-e651-42ed-ae81-bb3bd4aff2bc.png",
                    extractor.getSubChannelAvatarUrl());
        }
    }
}
