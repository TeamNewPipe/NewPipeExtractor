package org.schabi.newpipe.extractor.services.peertube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelExtractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmpty;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertGreaterOrEqual;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertTabsContain;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestImageCollection;

/**
 * Test for {@link PeertubeChannelExtractor}
 */
public class PeertubeChannelExtractorTest {

    public static class LaQuadratureDuNet implements BaseChannelExtractorTest {
        private static PeertubeChannelExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://framatube.org", "Framatube"));
            extractor = (PeertubeChannelExtractor) PeerTube
                    .getChannelExtractor("https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/videos");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testServiceId() {
            assertEquals(PeerTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testName() throws ParsingException {
            assertEquals("La Quadrature du Net", extractor.getName());
        }

        @Test
        public void testId() throws ParsingException {
            assertEquals("video-channels/lqdn_channel@video.lqdn.fr", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/videos", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ChannelExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testDescription() {
            assertNotNull(extractor.getDescription());
        }

        @Test
        void testParentChannelName() throws ParsingException {
            assertEquals("lqdn", extractor.getParentChannelName());
        }

        @Test
        void testParentChannelUrl() throws ParsingException {
            assertEquals("https://video.lqdn.fr/accounts/lqdn", extractor.getParentChannelUrl());
        }

        @Test
        void testParentChannelAvatarUrl() {
            defaultTestImageCollection(extractor.getParentChannelAvatars());
        }

        @Test
        public void testAvatars() {
            defaultTestImageCollection(extractor.getAvatars());
        }

        @Test
        public void testBanners() {
            assertEmpty(extractor.getBanners());
        }

        @Test
        public void testFeedUrl() throws ParsingException {
            assertEquals("https://framatube.org/feeds/videos.xml?videoChannelId=1126", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() {
            assertGreaterOrEqual(230, extractor.getSubscriberCount());
        }

        @Test
        @Override
        public void testVerified() throws Exception {
            assertFalse(extractor.isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            assertTabsContain(extractor.getTabs(), ChannelTabs.VIDEOS, ChannelTabs.PLAYLISTS);
        }

        @Test
        @Override
        public void testTags() throws Exception {
            assertTrue(extractor.getTags().isEmpty());
        }
    }

    public static class ChatSceptique implements BaseChannelExtractorTest {

        private static PeertubeChannelExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://framatube.org", "Framatube"));
            extractor = (PeertubeChannelExtractor) PeerTube
                    .getChannelExtractor("https://framatube.org/api/v1/video-channels/chatsceptique@skeptikon.fr");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testServiceId() {
            assertEquals(PeerTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testName() throws ParsingException {
            assertEquals("Chat Sceptique", extractor.getName());
        }

        @Test
        public void testId() throws ParsingException {
            assertEquals("video-channels/chatsceptique@skeptikon.fr", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://framatube.org/video-channels/chatsceptique@skeptikon.fr", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://framatube.org/api/v1/video-channels/chatsceptique@skeptikon.fr", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ChannelExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testDescription() {
            assertNotNull(extractor.getDescription());
        }

        @Test
        void testParentChannelName() throws ParsingException {
            assertEquals("nathan", extractor.getParentChannelName());
        }

        @Test
        void testParentChannelUrl() throws ParsingException {
            assertEquals("https://skeptikon.fr/accounts/nathan", extractor.getParentChannelUrl());
        }

        @Test
        void testParentChannelAvatars() {
            defaultTestImageCollection(extractor.getParentChannelAvatars());
        }

        @Test
        public void testAvatars() {
            defaultTestImageCollection(extractor.getAvatars());
        }

        @Test
        public void testBanners() throws ParsingException {
            assertEmpty(extractor.getBanners());
        }

        @Test
        public void testFeedUrl() throws ParsingException {
            assertEquals("https://framatube.org/feeds/videos.xml?videoChannelId=137", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() {
            assertGreaterOrEqual(700, extractor.getSubscriberCount());
        }

        @Test
        @Override
        public void testVerified() throws Exception {
            assertFalse(extractor.isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            assertTabsContain(extractor.getTabs(), ChannelTabs.VIDEOS, ChannelTabs.PLAYLISTS);
        }

        @Test
        @Override
        public void testTags() throws Exception {
            assertTrue(extractor.getTags().isEmpty());
        }
    }

    public static class DocumentaryChannel implements BaseChannelExtractorTest {
        // https://github.com/TeamNewPipe/NewPipe/issues/11369

        private static PeertubeChannelExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://kolektiva.media", "kolektiva.media"));
            extractor = (PeertubeChannelExtractor) PeerTube
                    .getChannelExtractor("https://kolektiva.media/video-channels/documentary_channel");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testServiceId() {
            assertEquals(PeerTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testName() throws ParsingException {
            assertEquals("Documentary Channel", extractor.getName());
        }

        @Test
        public void testId() throws ParsingException {
            assertEquals("video-channels/documentary_channel", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://kolektiva.media/video-channels/documentary_channel", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://kolektiva.media/video-channels/documentary_channel", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ChannelExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testDescription() {
            assertNotNull(extractor.getDescription());
        }

        @Test
        void testParentChannelName() throws ParsingException {
            assertEquals("consumedmind", extractor.getParentChannelName());
        }

        @Test
        void testParentChannelUrl() throws ParsingException {
            assertEquals("https://kolektiva.media/accounts/consumedmind", extractor.getParentChannelUrl());
        }

        @Test
        void testParentChannelAvatars() {
            defaultTestImageCollection(extractor.getParentChannelAvatars());
        }

        @Test
        public void testAvatars() {
            defaultTestImageCollection(extractor.getAvatars());
        }

        @Test
        public void testBanners() throws ParsingException {
            assertGreaterOrEqual(1, extractor.getBanners().size());
        }

        @Test
        public void testFeedUrl() throws ParsingException {
            assertEquals("https://kolektiva.media/feeds/videos.xml?videoChannelId=2994", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() {
            assertGreaterOrEqual(25, extractor.getSubscriberCount());
        }

        @Test
        @Override
        public void testVerified() throws Exception {
            assertFalse(extractor.isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            assertTabsContain(extractor.getTabs(), ChannelTabs.VIDEOS, ChannelTabs.PLAYLISTS);
        }

        @Test
        @Override
        public void testTags() throws Exception {
            assertTrue(extractor.getTags().isEmpty());
        }
    }
}
