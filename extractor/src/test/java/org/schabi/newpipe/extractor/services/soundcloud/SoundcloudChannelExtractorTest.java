package org.schabi.newpipe.extractor.services.soundcloud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmpty;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertTabsContain;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestImageCollection;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudChannelExtractor;

/**
 * Test for {@link SoundcloudChannelExtractor}
 */
public class SoundcloudChannelExtractorTest {
    static abstract class Base extends DefaultSimpleExtractorTest<ChannelExtractor>
        implements BaseChannelExtractorTest {

        @Override
        protected ChannelExtractor createExtractor() throws Exception {
            return SoundCloud.getChannelExtractor(extractorUrl());
        }

        protected abstract String extractorUrl();
    }

    public static class LilUzi extends Base {

        @Override
        protected String extractorUrl() {
            return "http://soundcloud.com/liluzivert/sets";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(SoundCloud.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws ParsingException {
            assertEquals("Lil Uzi Vert", extractor().getName());
        }

        @Override
        @Test
        public void testId() throws ParsingException {
            assertEquals("10494998", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://soundcloud.com/liluzivert", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("http://soundcloud.com/liluzivert/sets", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testDescription() throws ParsingException {
            assertNotNull(extractor().getDescription());
        }

        @Override
        @Test
        public void testAvatars() throws ParsingException {
            defaultTestImageCollection(extractor().getAvatars());
        }

        @Override
        @Test
        public void testBanners() throws ParsingException {
            defaultTestImageCollection(extractor().getBanners());
        }

        @Override
        @Test
        public void testFeedUrl() throws ParsingException {
            assertEmpty(extractor().getFeedUrl());
        }

        @Override
        @Test
        public void testSubscriberCount() throws ParsingException {
            assertTrue(extractor().getSubscriberCount() >= 1e6, "Wrong subscriber count");
        }

        @Override
        public void testVerified() throws Exception {
            assertTrue(extractor().isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            assertTabsContain(extractor().getTabs(), ChannelTabs.TRACKS, ChannelTabs.PLAYLISTS,
                    ChannelTabs.ALBUMS, ChannelTabs.LIKES);
        }

        @Test
        @Override
        public void testTags() throws Exception {
            assertTrue(extractor().getTags().isEmpty());
        }
    }

    public static class DubMatix extends Base {

        @Override
        protected String extractorUrl() {
            return "https://soundcloud.com/dubmatix";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(SoundCloud.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws ParsingException {
            assertEquals("dubmatix", extractor().getName());
        }

        @Override
        @Test
        public void testId() throws ParsingException {
            assertEquals("542134", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://soundcloud.com/dubmatix", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://soundcloud.com/dubmatix", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testDescription() throws ParsingException {
            assertNotNull(extractor().getDescription());
        }

        @Override
        @Test
        public void testAvatars() throws ParsingException {
            defaultTestImageCollection(extractor().getAvatars());
        }

        @Override
        @Test
        public void testBanners() throws ParsingException {
            defaultTestImageCollection(extractor().getBanners());
        }

        @Override
        @Test
        public void testFeedUrl() throws ParsingException {
            assertEmpty(extractor().getFeedUrl());
        }

        @Override
        @Test
        public void testSubscriberCount() throws ParsingException {
            assertTrue(extractor().getSubscriberCount() >= 2e6, "Wrong subscriber count");
        }

        @Override
        public void testVerified() throws Exception {
            assertTrue(extractor().isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            assertTabsContain(extractor().getTabs(), ChannelTabs.TRACKS, ChannelTabs.PLAYLISTS,
                    ChannelTabs.ALBUMS, ChannelTabs.LIKES);
        }

        @Test
        @Override
        public void testTags() throws Exception {
            assertTrue(extractor().getTags().isEmpty());
        }
    }
}
