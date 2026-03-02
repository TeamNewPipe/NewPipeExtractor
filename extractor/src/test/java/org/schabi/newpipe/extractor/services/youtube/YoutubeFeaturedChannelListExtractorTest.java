package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertContains;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestMoreItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.channel.list.ChannelListExtractor;
import org.schabi.newpipe.extractor.channel.tabs.rendererlist.RendererListInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.services.BaseListExtractorTest;
import org.schabi.newpipe.extractor.services.BasePlaylistExtractorTest;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeFeaturedChannelListExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubePlaylistExtractor;
import org.schabi.newpipe.extractor.stream.Description;

import java.util.List;

/**
 * Test for {@link YoutubeFeaturedChannelListExtractor}
 */
public class YoutubeFeaturedChannelListExtractorTest {

    public static class NotAvailable implements InitYoutubeTest {

        @Test
        void invalidIndex() throws Exception {
            final ChannelListExtractor extractor =
                    YouTube.getChannelListExtractor(
                            "user/LinusTechTips",
                            List.of("featured", RendererListInfoItemExtractor
                                    .getRendererListIndexContentFilter(2)),
                            "https://www.youtube.com");
            assertThrows(ExtractionException.class, extractor::fetchPage);
        }
    }

    abstract static class Base extends DefaultSimpleExtractorTest<YoutubeFeaturedChannelListExtractor>
            implements BaseListExtractorTest, InitYoutubeTest {

        @Override
        protected YoutubeFeaturedChannelListExtractor createExtractor() throws Exception {
            return (YoutubeFeaturedChannelListExtractor) YouTube.getChannelListExtractor(
                    this.idForExtraction(),
                    List.of("featured", RendererListInfoItemExtractor
                            .getRendererListIndexContentFilter(this.rendererListIndexForExtraction())),
                    "https://www.youtube.com");
        }

        protected abstract int rendererListIndexForExtraction();

        protected abstract String idForExtraction();
    }

    public static class LinusTechTips extends YoutubeFeaturedChannelListExtractorTest.Base {
        @Override
        protected int rendererListIndexForExtraction() {
            return 5;
        }

        @Override
        protected String idForExtraction() {
            return "user/LinusTechTips";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws Exception {
            assertTrue(extractor().getName().startsWith("Featured Channels"));
        }

        @Override
        @Test
        public void testId() throws Exception {
            assertEquals("UCXuqSBlHAE6Xw-yeJA0Tunw", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCXuqSBlHAE6Xw-yeJA0Tunw/featured", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/user/LinusTechTips/featured", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor());
        }

        @Override
        public void testMoreRelatedItems() throws Exception {
            assertFalse(extractor().getInitialPage().hasNextPage());
        }
    }

    public static class TSeries extends YoutubeFeaturedChannelListExtractorTest.Base {
        @Override
        protected int rendererListIndexForExtraction() {
            return 12;
        }

        @Override
        protected String idForExtraction() {
            return "user/tseries";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws Exception {
            assertTrue(extractor().getName().startsWith("Other Great Channels"));
        }

        @Override
        @Test
        public void testId() throws Exception {
            assertEquals("UCq-Fj5jknLsUf-MWSy4_brA", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCq-Fj5jknLsUf-MWSy4_brA/featured", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/user/tseries/featured", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor());
        }

        @Override
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor());
        }
    }
}
