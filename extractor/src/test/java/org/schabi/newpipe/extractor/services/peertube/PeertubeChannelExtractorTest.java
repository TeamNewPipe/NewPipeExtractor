package org.schabi.newpipe.extractor.services.peertube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmpty;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertGreaterOrEqual;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertTabsContain;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestImageCollection;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelExtractor;

/**
 * Test for {@link PeertubeChannelExtractor}
 */
public class PeertubeChannelExtractorTest {

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

    public static class LaQuadratureDuNet extends Base {
        @Override
        protected PeertubeInstance peertubeInstance() {
            return new PeertubeInstance("https://framatube.org", "Framatube");
        }

        @Override
        protected String extractorUrl() {
            return "https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/videos";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(PeerTube.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws ParsingException {
            assertEquals("La Quadrature du Net", extractor().getName());
        }

        @Override
        @Test
        public void testId() throws ParsingException {
            assertEquals("video-channels/lqdn_channel@video.lqdn.fr", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/videos", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testDescription() throws ParsingException {
            assertNotNull(extractor().getDescription());
        }

        @Test
        void testParentChannelName() throws ParsingException {
            assertEquals("lqdn", extractor().getParentChannelName());
        }

        @Test
        void testParentChannelUrl() throws ParsingException {
            assertEquals("https://video.lqdn.fr/accounts/lqdn", extractor().getParentChannelUrl());
        }

        @Test
        void testParentChannelAvatarUrl() throws ParsingException {
            defaultTestImageCollection(extractor().getParentChannelAvatars());
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
            assertEquals("https://framatube.org/feeds/videos.xml?videoChannelId=1126", extractor().getFeedUrl());
        }

        @Override
        @Test
        public void testSubscriberCount() throws ParsingException {
            assertGreaterOrEqual(230, extractor().getSubscriberCount());
        }

        @Test
        @Override
        public void testVerified() throws Exception {
            assertFalse(extractor().isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            assertTabsContain(extractor().getTabs(), ChannelTabs.VIDEOS, ChannelTabs.PLAYLISTS);
        }

        @Test
        @Override
        public void testTags() throws Exception {
            assertTrue(extractor().getTags().isEmpty());
        }
    }

    public static class ChatSceptique extends Base {
        @Override
        protected PeertubeInstance peertubeInstance() {
            return new PeertubeInstance("https://framatube.org", "Framatube");
        }

        @Override
        protected String extractorUrl() {
            return "https://framatube.org/api/v1/video-channels/chatsceptique@skeptikon.fr";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(PeerTube.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws ParsingException {
            assertEquals("Chat Sceptique", extractor().getName());
        }

        @Override
        @Test
        public void testId() throws ParsingException {
            assertEquals("video-channels/chatsceptique@skeptikon.fr", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://framatube.org/video-channels/chatsceptique@skeptikon.fr", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://framatube.org/api/v1/video-channels/chatsceptique@skeptikon.fr", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testDescription() throws ParsingException {
            assertNotNull(extractor().getDescription());
        }

        @Test
        void testParentChannelName() throws ParsingException {
            assertEquals("nathan", extractor().getParentChannelName());
        }

        @Test
        void testParentChannelUrl() throws ParsingException {
            assertEquals("https://skeptikon.fr/accounts/nathan", extractor().getParentChannelUrl());
        }

        @Test
        void testParentChannelAvatars() throws ParsingException {
            defaultTestImageCollection(extractor().getParentChannelAvatars());
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
            assertEquals("https://framatube.org/feeds/videos.xml?videoChannelId=137", extractor().getFeedUrl());
        }

        @Override
        @Test
        public void testSubscriberCount() throws ParsingException {
            assertGreaterOrEqual(700, extractor().getSubscriberCount());
        }

        @Test
        @Override
        public void testVerified() throws Exception {
            assertFalse(extractor().isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            assertTabsContain(extractor().getTabs(), ChannelTabs.VIDEOS, ChannelTabs.PLAYLISTS);
        }

        @Test
        @Override
        public void testTags() throws Exception {
            assertTrue(extractor().getTags().isEmpty());
        }
    }

    public static class DocumentaryChannel extends Base {
        // https://github.com/TeamNewPipe/NewPipe/issues/11369

        @Override
        protected PeertubeInstance peertubeInstance() {
            return new PeertubeInstance("https://kolektiva.media", "kolektiva.media");
        }

        @Override
        protected String extractorUrl() {
            return "https://kolektiva.media/video-channels/documentary_channel";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(PeerTube.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws ParsingException {
            assertEquals("Documentary Channel", extractor().getName());
        }

        @Override
        @Test
        public void testId() throws ParsingException {
            assertEquals("video-channels/documentary_channel", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://kolektiva.media/video-channels/documentary_channel", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://kolektiva.media/video-channels/documentary_channel", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testDescription() throws ParsingException {
            assertNotNull(extractor().getDescription());
        }

        @Test
        void testParentChannelName() throws ParsingException {
            assertEquals("consumedmind", extractor().getParentChannelName());
        }

        @Test
        void testParentChannelUrl() throws ParsingException {
            assertEquals("https://kolektiva.media/accounts/consumedmind", extractor().getParentChannelUrl());
        }

        @Test
        void testParentChannelAvatars() throws ParsingException {
            defaultTestImageCollection(extractor().getParentChannelAvatars());
        }

        @Override
        @Test
        public void testAvatars() throws ParsingException {
            defaultTestImageCollection(extractor().getAvatars());
        }

        @Override
        @Test
        public void testBanners() throws ParsingException {
            assertGreaterOrEqual(1, extractor().getBanners().size());
        }

        @Override
        @Test
        public void testFeedUrl() throws ParsingException {
            assertEquals("https://kolektiva.media/feeds/videos.xml?videoChannelId=2994", extractor().getFeedUrl());
        }

        @Override
        @Test
        public void testSubscriberCount() throws ParsingException {
            assertGreaterOrEqual(25, extractor().getSubscriberCount());
        }

        @Test
        @Override
        public void testVerified() throws Exception {
            assertFalse(extractor().isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            assertTabsContain(extractor().getTabs(), ChannelTabs.VIDEOS, ChannelTabs.PLAYLISTS);
        }

        @Test
        @Override
        public void testTags() throws Exception {
            assertTrue(extractor().getTags().isEmpty());
        }
    }
}
