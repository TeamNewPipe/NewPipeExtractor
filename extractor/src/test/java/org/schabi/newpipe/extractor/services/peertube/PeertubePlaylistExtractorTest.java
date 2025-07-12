package org.schabi.newpipe.extractor.services.peertube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestImageCollection;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;

public class PeertubePlaylistExtractorTest {

    public static class Shocking extends DefaultSimpleExtractorTest<PlaylistExtractor> {
        @Override
        protected PlaylistExtractor createExtractor() throws Exception {
            return PeerTube.getPlaylistExtractor(
                "https://framatube.org/videos/watch/playlist/96b0ee2b-a5a7-4794-8769-58d8ccb79ab7");
        }

        @Test
        void testGetName() throws ParsingException {
            assertEquals("Shocking !", extractor().getName());
        }

        @Test
        void testGetThumbnails() throws ParsingException {
            defaultTestImageCollection(extractor().getThumbnails());
        }

        @Test
        void testGetUploaderUrl() throws ParsingException {
            assertEquals("https://skeptikon.fr/accounts/metadechoc", extractor().getUploaderUrl());
        }

        @Test
        void testGetUploaderAvatars() throws ParsingException {
            defaultTestImageCollection(extractor().getUploaderAvatars());
        }

        @Test
        void testGetUploaderName() throws ParsingException {
            assertEquals("Méta de Choc", extractor().getUploaderName());
        }

        @Test
        void testGetStreamCount() throws ParsingException {
            ExtractorAsserts.assertGreaterOrEqual(39, extractor().getStreamCount());
        }

        @Test
        void testGetDescription() throws ParsingException {
            ExtractorAsserts.assertContains("épisodes de Shocking", extractor().getDescription().getContent());
        }

        @Test
        void testGetSubChannelUrl() throws ParsingException {
            assertEquals("https://skeptikon.fr/video-channels/metadechoc_channel", extractor().getSubChannelUrl());
        }

        @Test
        void testGetSubChannelName() throws ParsingException {
            assertEquals("SHOCKING !", extractor().getSubChannelName());
        }

        @Test
        void testGetSubChannelAvatars() throws ParsingException {
            defaultTestImageCollection(extractor().getSubChannelAvatars());
        }
    }
}
