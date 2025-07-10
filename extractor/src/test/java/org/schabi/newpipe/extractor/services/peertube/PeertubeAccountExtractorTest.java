package org.schabi.newpipe.extractor.services.peertube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmpty;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertTabsContain;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestImageCollection;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeAccountExtractor;

/**
 * Test for {@link PeertubeAccountExtractor}
 */
public class PeertubeAccountExtractorTest {

    static abstract class Base extends DefaultSimpleExtractorTest<ChannelExtractor>
        implements BaseChannelExtractorTest {

        @Override
        protected ChannelExtractor createExtractor() throws Exception {
            // setting instance might break test when running in parallel
            PeerTube.setInstance(peertubeInstance());
            return PeerTube.getChannelExtractor(extractorUrl());
        }

        protected abstract PeertubeInstance peertubeInstance();

        protected abstract String extractorUrl();
    }

    public static class Framasoft extends Base {

        @Override
        protected PeertubeInstance peertubeInstance() {
            return new PeertubeInstance("https://framatube.org", "Framatube");
        }

        @Override
        protected String extractorUrl() {
            return "https://framatube.org/accounts/framasoft";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(PeerTube.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws ParsingException {
            assertEquals("Framasoft", extractor().getName());
        }

        @Override
        @Test
        public void testId() throws ParsingException {
            assertEquals("accounts/framasoft", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://framatube.org/accounts/framasoft", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://framatube.org/accounts/framasoft", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testDescription() throws ParsingException {
            assertNull(extractor().getDescription());
        }

        @Override
        @Test
        public void testAvatars() throws ParsingException {
            defaultTestImageCollection(extractor().getAvatars());
        }

        @Override
        @Test
        public void testBanners() throws ParsingException {
            assertEmpty(extractor().getBanners());
        }

        @Override
        @Test
        public void testFeedUrl() throws ParsingException {
            assertEquals("https://framatube.org/feeds/videos.xml?accountId=3", extractor().getFeedUrl());
        }

        @Override
        @Test
        public void testSubscriberCount() throws ParsingException {
            ExtractorAsserts.assertGreaterOrEqual(700, extractor().getSubscriberCount());
        }

        @Test
        @Override
        public void testVerified() throws Exception {
            assertFalse(extractor().isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            assertTabsContain(extractor().getTabs(), ChannelTabs.VIDEOS, ChannelTabs.CHANNELS);
        }

        @Test
        @Override
        public void testTags() throws Exception {
            assertTrue(extractor().getTags().isEmpty());
        }
    }

    public static class FreeSoftwareFoundation extends Base {

        @Override
        protected PeertubeInstance peertubeInstance() {
            return new PeertubeInstance("https://framatube.org", "Framatube");
        }

        @Override
        protected String extractorUrl() {
            return "https://framatube.org/api/v1/accounts/fsf";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(PeerTube.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws ParsingException {
            assertEquals("Free Software Foundation", extractor().getName());
        }

        @Override
        @Test
        public void testId() throws ParsingException {
            assertEquals("accounts/fsf", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://framatube.org/accounts/fsf", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://framatube.org/api/v1/accounts/fsf", extractor().getOriginalUrl());
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
            assertEmpty(extractor().getBanners());
        }

        @Override
        @Test
        public void testFeedUrl() throws ParsingException {
            assertEquals("https://framatube.org/feeds/videos.xml?accountId=8178", extractor().getFeedUrl());
        }

        @Override
        @Test
        public void testSubscriberCount() throws ParsingException {
            ExtractorAsserts.assertGreaterOrEqual(100, extractor().getSubscriberCount());
        }

        @Test
        @Override
        public void testVerified() throws Exception {
            assertFalse(extractor().isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            assertTabsContain(extractor().getTabs(), ChannelTabs.VIDEOS, ChannelTabs.CHANNELS);
        }

        @Test
        @Override
        public void testTags() throws Exception {
            assertTrue(extractor().getTags().isEmpty());
        }
    }
}
