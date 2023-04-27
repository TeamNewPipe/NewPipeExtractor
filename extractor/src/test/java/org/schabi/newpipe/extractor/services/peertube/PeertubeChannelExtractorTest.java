package org.schabi.newpipe.extractor.services.peertube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelExtractor;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.*;

/**
 * Test for {@link PeertubeChannelExtractor}
 */
public class PeertubeChannelExtractorTest {

    public static class LaQuadratureDuNet implements BaseChannelExtractorTest {
        private static PeertubeChannelExtractor extractor;
        private static ChannelTabExtractor tabExtractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://framatube.org", "Framatube"));
            extractor = (PeertubeChannelExtractor) PeerTube
                    .getChannelExtractor("https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/videos");
            extractor.fetchPage();

            tabExtractor = PeerTube.getChannelTabExtractor(extractor.getTabs().get(0));
            tabExtractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testServiceId() {
            assertEquals(PeerTube.getServiceId(), extractor.getServiceId());
            assertEquals(PeerTube.getServiceId(), tabExtractor.getServiceId());
        }

        @Test
        public void testName() throws ParsingException {
            assertEquals("La Quadrature du Net", extractor.getName());
        }

        @Test
        public void testId() throws ParsingException {
            assertEquals("video-channels/lqdn_channel@video.lqdn.fr", extractor.getId());
            assertEquals("video-channels/lqdn_channel@video.lqdn.fr", tabExtractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr", extractor.getUrl());
            assertEquals("https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/videos", tabExtractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/videos", extractor.getOriginalUrl());
            assertEquals("https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/videos", tabExtractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(tabExtractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(tabExtractor);
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ChannelExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testDescription() throws ParsingException {
            assertNotNull(extractor.getDescription());
        }

        @Test
        public void testParentChannelName() throws ParsingException {
            assertEquals("lqdn", extractor.getParentChannelName());
        }

        @Test
        public void testParentChannelUrl() throws ParsingException {
            assertEquals("https://video.lqdn.fr/accounts/lqdn", extractor.getParentChannelUrl());
        }

        @Test
        public void testParentChannelAvatarUrl() throws ParsingException {
            assertIsSecureUrl(extractor.getParentChannelAvatarUrl());
        }

        @Test
        public void testAvatarUrl() throws ParsingException {
            assertIsSecureUrl(extractor.getAvatarUrl());
        }

        @Test
        public void testBannerUrl() throws ParsingException {
            assertNull(extractor.getBannerUrl());
        }

        @Test
        public void testFeedUrl() throws ParsingException {
            assertEquals("https://framatube.org/feeds/videos.xml?videoChannelId=1126", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() throws ParsingException {
            ExtractorAsserts.assertGreaterOrEqual(230, extractor.getSubscriberCount());
        }

        @Test
        public void testVerified() throws Exception {
            assertFalse(extractor.isVerified());
        }

        @Test
        public void testTabs() throws Exception {
            Set<String> tabs = extractor.getTabs().stream()
                    .map(linkHandler -> linkHandler.getContentFilters().get(0)).collect(Collectors.toSet());
            assertTrue(tabs.contains(ChannelTabs.PLAYLISTS));
        }
    }

    public static class ChatSceptique implements BaseChannelExtractorTest {

        private static PeertubeChannelExtractor extractor;
        private static ChannelTabExtractor tabExtractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://framatube.org", "Framatube"));
            extractor = (PeertubeChannelExtractor) PeerTube
                    .getChannelExtractor("https://framatube.org/api/v1/video-channels/chatsceptique@skeptikon.fr");
            extractor.fetchPage();

            tabExtractor = PeerTube.getChannelTabExtractor(extractor.getTabs().get(0));
            tabExtractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Additional Testing
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testGetPageInNewExtractor() throws Exception {
            final ChannelExtractor newExtractor = PeerTube.getChannelExtractor(extractor.getUrl());
            newExtractor.fetchPage();
            final ChannelTabExtractor newTabExtractor = PeerTube.getChannelTabExtractor(newExtractor.getTabs().get(0));
            defaultTestGetPageInNewExtractor(tabExtractor, newTabExtractor);
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testServiceId() {
            assertEquals(PeerTube.getServiceId(), extractor.getServiceId());
            assertEquals(PeerTube.getServiceId(), tabExtractor.getServiceId());
        }

        @Test
        public void testName() throws ParsingException {
            assertEquals("Chat Sceptique", extractor.getName());
        }

        @Test
        public void testId() throws ParsingException {
            assertEquals("video-channels/chatsceptique@skeptikon.fr", extractor.getId());
            assertEquals("video-channels/chatsceptique@skeptikon.fr", tabExtractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://framatube.org/video-channels/chatsceptique@skeptikon.fr", extractor.getUrl());
            assertEquals("https://framatube.org/video-channels/chatsceptique@skeptikon.fr/videos", tabExtractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://framatube.org/api/v1/video-channels/chatsceptique@skeptikon.fr", extractor.getOriginalUrl());
            assertEquals("https://framatube.org/video-channels/chatsceptique@skeptikon.fr/videos", tabExtractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(tabExtractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(tabExtractor);
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ChannelExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testDescription() throws ParsingException {
            assertNotNull(extractor.getDescription());
        }

        @Test
        public void testParentChannelName() throws ParsingException {
            assertEquals("nathan", extractor.getParentChannelName());
        }

        @Test
        public void testParentChannelUrl() throws ParsingException {
            assertEquals("https://skeptikon.fr/accounts/nathan", extractor.getParentChannelUrl());
        }

        @Test
        public void testParentChannelAvatarUrl() throws ParsingException {
            assertIsSecureUrl(extractor.getParentChannelAvatarUrl());
        }

        @Test
        public void testAvatarUrl() throws ParsingException {
            assertIsSecureUrl(extractor.getAvatarUrl());
        }

        @Test
        public void testBannerUrl() throws ParsingException {
            assertNull(extractor.getBannerUrl());
        }

        @Test
        public void testFeedUrl() throws ParsingException {
            assertEquals("https://framatube.org/feeds/videos.xml?videoChannelId=137", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() throws ParsingException {
            ExtractorAsserts.assertGreaterOrEqual(700, extractor.getSubscriberCount());
        }

        @Test
        public void testVerified() throws Exception {
            assertFalse(extractor.isVerified());
        }

        @Test
        public void testTabs() throws Exception {
            Set<String> tabs = extractor.getTabs().stream()
                    .map(linkHandler -> linkHandler.getContentFilters().get(0)).collect(Collectors.toSet());
            assertTrue(tabs.contains(ChannelTabs.PLAYLISTS));
        }
    }
}
