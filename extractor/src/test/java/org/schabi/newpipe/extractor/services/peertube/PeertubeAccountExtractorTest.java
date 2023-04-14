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
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeAccountExtractor;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.*;

/**
 * Test for {@link PeertubeAccountExtractor}
 */
public class PeertubeAccountExtractorTest {

    public static class Framasoft implements BaseChannelExtractorTest {
        private static PeertubeAccountExtractor extractor;
        private static ChannelTabExtractor tabExtractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://framatube.org", "Framatube"));
            extractor = (PeertubeAccountExtractor) PeerTube
                    .getChannelExtractor("https://framatube.org/accounts/framasoft");
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
        }

        @Test
        public void testName() throws ParsingException {
            assertEquals("Framasoft", extractor.getName());
        }

        @Test
        public void testId() throws ParsingException {
            assertEquals("accounts/framasoft", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://framatube.org/accounts/framasoft", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://framatube.org/accounts/framasoft", extractor.getOriginalUrl());
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
        public void testAvatarUrl() throws ParsingException {
            assertIsSecureUrl(extractor.getAvatarUrl());
        }

        @Test
        public void testBannerUrl() {
            assertNull(extractor.getBannerUrl());
        }

        @Test
        public void testFeedUrl() throws ParsingException {
            assertEquals("https://framatube.org/feeds/videos.xml?accountId=3", extractor.getFeedUrl());
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
            assertTrue(tabs.contains(ChannelTabs.VIDEOS));
            assertTrue(tabs.contains(ChannelTabs.CHANNELS));
        }
    }

    public static class FreeSoftwareFoundation implements BaseChannelExtractorTest {
        private static PeertubeAccountExtractor extractor;
        private static ChannelTabExtractor tabExtractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://framatube.org", "Framatube"));
            extractor = (PeertubeAccountExtractor) PeerTube
                    .getChannelExtractor("https://framatube.org/api/v1/accounts/fsf");
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
        }

        @Test
        public void testName() throws ParsingException {
            assertEquals("Free Software Foundation", extractor.getName());
        }

        @Test
        public void testId() throws ParsingException {
            assertEquals("accounts/fsf", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://framatube.org/accounts/fsf", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://framatube.org/api/v1/accounts/fsf", extractor.getOriginalUrl());
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
        public void testAvatarUrl() throws ParsingException {
            assertIsSecureUrl(extractor.getAvatarUrl());
        }

        @Test
        public void testBannerUrl() throws ParsingException {
            assertNull(extractor.getBannerUrl());
        }

        @Test
        public void testFeedUrl() throws ParsingException {
            assertEquals("https://framatube.org/feeds/videos.xml?accountId=8178", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() throws ParsingException {
            ExtractorAsserts.assertGreaterOrEqual(100, extractor.getSubscriberCount());
        }

        @Test
        public void testVerified() throws Exception {
            assertFalse(extractor.isVerified());
        }

        @Test
        public void testTabs() throws Exception {
            Set<String> tabs = extractor.getTabs().stream()
                    .map(linkHandler -> linkHandler.getContentFilters().get(0)).collect(Collectors.toSet());
            assertTrue(tabs.contains(ChannelTabs.VIDEOS));
            assertTrue(tabs.contains(ChannelTabs.CHANNELS));
        }
    }
}
