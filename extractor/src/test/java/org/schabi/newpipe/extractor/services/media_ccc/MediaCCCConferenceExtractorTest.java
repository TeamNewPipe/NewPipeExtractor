package org.schabi.newpipe.extractor.services.media_ccc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertContainsImageUrlInImageCollection;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCConferenceExtractor;

/**
 * Test {@link MediaCCCConferenceExtractor} and {@link
 * org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCChannelTabExtractor}
 */
public class MediaCCCConferenceExtractorTest {

    abstract static class Base extends DefaultSimpleExtractorTest<ChannelExtractor> {
        protected ChannelTabExtractor tabExtractor;

        @Override
        protected void fetchExtractor(final ChannelExtractor extractor) throws Exception {
            super.fetchExtractor(extractor);

            tabExtractor = MediaCCC.getChannelTabExtractor(extractor.getTabs().get(0));
            tabExtractor.fetchPage();
        }
    }

    public static class FrOSCon2017 extends Base {
        @Override
        protected ChannelExtractor createExtractor() throws Exception {
            return MediaCCC.getChannelExtractor("https://media.ccc.de/c/froscon2017");
        }

        @Test
        void testName() throws Exception {
            assertEquals("FrOSCon 2017", extractor().getName());
        }

        @Test
        void testGetUrl() throws Exception {
            assertEquals("https://media.ccc.de/c/froscon2017", extractor().getUrl());
        }

        @Test
        void testGetOriginalUrl() throws Exception {
            assertEquals("https://media.ccc.de/c/froscon2017", extractor().getOriginalUrl());
        }

        @Test
        void testGetThumbnails() throws ParsingException {
            assertContainsImageUrlInImageCollection(
                    "https://static.media.ccc.de/media/events/froscon/2017/logo.png",
                extractor().getAvatars());
        }

        @Test
        void testGetInitalPage() throws Exception {
            extractor(); // Init extractor
            assertEquals(97, tabExtractor.getInitialPage().getItems().size());
        }
    }

    public static class Oscal2019 extends Base {
        @Override
        protected ChannelExtractor createExtractor() throws Exception {
            return MediaCCC.getChannelExtractor("https://media.ccc.de/c/oscal19");
        }

        @Test
        void testName() throws Exception {
            assertEquals("Open Source Conference Albania 2019", extractor().getName());
        }

        @Test
        void testGetUrl() throws Exception {
            assertEquals("https://media.ccc.de/c/oscal19", extractor().getUrl());
        }

        @Test
        void testGetOriginalUrl() throws Exception {
            assertEquals("https://media.ccc.de/c/oscal19", extractor().getOriginalUrl());
        }

        @Test
        void testGetThumbnailUrl() throws ParsingException {
            assertContainsImageUrlInImageCollection(
                    "https://static.media.ccc.de/media/events/oscal/2019/oscal-19.png",
                extractor().getAvatars());
        }

        @Test
        void testGetInitalPage() throws Exception {
            extractor(); // Init extractor
            assertTrue(tabExtractor.getInitialPage().getItems().size() >= 21);
        }
    }
}
