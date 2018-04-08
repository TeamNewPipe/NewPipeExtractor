package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.*;

/**
 * Test for {@link ChannelExtractor}
 */
public class YoutubeChannelExtractorTest {
    public static class Gronkh implements BaseChannelExtractorTest {
        private static YoutubeChannelExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(Downloader.getInstance());
            extractor = (YoutubeChannelExtractor) YouTube
                    .getChannelExtractor("http://www.youtube.com/user/Gronkh");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testName() throws Exception {
            assertEquals("Gronkh", extractor.getName());
        }

        @Test
        public void testId() throws Exception {
            assertEquals("UCYJ61XIK64sp6ZFFS8sctxw", extractor.getId());
        }

        @Test
        public void testCleanUrl() {
            assertEquals("https://www.youtube.com/channel/UCYJ61XIK64sp6ZFFS8sctxw", extractor.getCleanUrl());
        }

        @Test
        public void testOriginalUrl() {
            assertEquals("http://www.youtube.com/user/Gronkh", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor, YouTube.getServiceId());
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor, ServiceList.YouTube.getServiceId());
        }

         /*//////////////////////////////////////////////////////////////////////////
         // ChannelExtractor
         //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testDescription() throws Exception {
            assertTrue(extractor.getDescription().contains("Zart im Schmelz und süffig im Abgang. Ungebremster Spieltrieb"));
        }

        @Test
        public void testAvatarUrl() throws Exception {
            String avatarUrl = extractor.getAvatarUrl();
            assertIsSecureUrl(avatarUrl);
            assertTrue(avatarUrl, avatarUrl.contains("yt3"));
        }

        @Test
        public void testBannerUrl() throws Exception {
            String bannerUrl = extractor.getBannerUrl();
            assertIsSecureUrl(bannerUrl);
            assertTrue(bannerUrl, bannerUrl.contains("yt3"));
        }

        @Test
        public void testFeedUrl() throws Exception {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCYJ61XIK64sp6ZFFS8sctxw", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() throws Exception {
            assertTrue("Wrong subscriber count", extractor.getSubscriberCount() >= 0);
        }

        @Test
        public void testChannelDonation() throws Exception {
            // this needs to be ignored since wed have to upgrade channel extractor to the new yt interface
            // in order to make this work
            assertTrue(extractor.getDonationLinks().length == 0);
        }
    }

    public static class Kurzgesagt implements BaseChannelExtractorTest {
        private static YoutubeChannelExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(Downloader.getInstance());
            extractor = (YoutubeChannelExtractor) YouTube
                    .getChannelExtractor("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Additional Testing
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testGetPageInNewExtractor() throws Exception {
            final ChannelExtractor newExtractor = YouTube.getChannelExtractor(extractor.getCleanUrl());
            defaultTestGetPageInNewExtractor(extractor, newExtractor, YouTube.getServiceId());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testName() throws Exception {
            String name = extractor.getName();
            assertTrue(name, name.startsWith("Kurzgesagt"));
        }

        @Test
        public void testId() throws Exception {
            assertEquals("UCsXVk37bltHxD1rDPwtNM8Q", extractor.getId());
        }

        @Test
        public void testCleanUrl() {
            assertEquals("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q", extractor.getCleanUrl());
        }

        @Test
        public void testOriginalUrl() {
            assertEquals("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor, YouTube.getServiceId());
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor, ServiceList.YouTube.getServiceId());
        }

         /*//////////////////////////////////////////////////////////////////////////
         // ChannelExtractor
         //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testDescription() throws Exception {
            final String description = extractor.getDescription();
            assertTrue(description, description.contains("small team who want to make science look beautiful"));
            //TODO: Description get cuts out, because the og:description is optimized and don't have all the content
            //assertTrue(description, description.contains("Currently we make one animation video per month"));
        }

        @Test
        public void testAvatarUrl() throws Exception {
            String avatarUrl = extractor.getAvatarUrl();
            assertIsSecureUrl(avatarUrl);
            assertTrue(avatarUrl, avatarUrl.contains("yt3"));
        }

        @Test
        public void testBannerUrl() throws Exception {
            String bannerUrl = extractor.getBannerUrl();
            assertIsSecureUrl(bannerUrl);
            assertTrue(bannerUrl, bannerUrl.contains("yt3"));
        }

        @Test
        public void testFeedUrl() throws Exception {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCsXVk37bltHxD1rDPwtNM8Q", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() throws Exception {
            assertTrue("Wrong subscriber count", extractor.getSubscriberCount() >= 5e6);
        }

        @Test
        public void testChannelDonation() throws Exception {
            assertTrue(extractor.getDonationLinks().length == 1);
        }
    }

    public static class CaptainDisillusion implements BaseChannelExtractorTest {
        private static YoutubeChannelExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(Downloader.getInstance());
            extractor = (YoutubeChannelExtractor) YouTube
                    .getChannelExtractor("https://www.youtube.com/user/CaptainDisillusion/videos");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testName() throws Exception {
            assertEquals("CaptainDisillusion", extractor.getName());
        }

        @Test
        public void testId() throws Exception {
            assertEquals("UCEOXxzW2vU0P-0THehuIIeg", extractor.getId());
        }

        @Test
        public void testCleanUrl() {
            assertEquals("https://www.youtube.com/channel/UCEOXxzW2vU0P-0THehuIIeg", extractor.getCleanUrl());
        }

        @Test
        public void testOriginalUrl() {
            assertEquals("https://www.youtube.com/user/CaptainDisillusion/videos", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor, YouTube.getServiceId());
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor, ServiceList.YouTube.getServiceId());
        }

         /*//////////////////////////////////////////////////////////////////////////
         // ChannelExtractor
         //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testDescription() throws Exception {
            final String description = extractor.getDescription();
            assertTrue(description, description.contains("In a world where"));
        }

        @Test
        public void testAvatarUrl() throws Exception {
            String avatarUrl = extractor.getAvatarUrl();
            assertIsSecureUrl(avatarUrl);
            assertTrue(avatarUrl, avatarUrl.contains("yt3"));
        }

        @Test
        public void testBannerUrl() throws Exception {
            String bannerUrl = extractor.getBannerUrl();
            assertIsSecureUrl(bannerUrl);
            assertTrue(bannerUrl, bannerUrl.contains("yt3"));
        }

        @Test
        public void testFeedUrl() throws Exception {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCEOXxzW2vU0P-0THehuIIeg", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() throws Exception {
            assertTrue("Wrong subscriber count", extractor.getSubscriberCount() >= 5e5);
        }
    }

    public static class RandomChannel implements BaseChannelExtractorTest {
        private static YoutubeChannelExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(Downloader.getInstance());
            extractor = (YoutubeChannelExtractor) YouTube
                    .getChannelExtractor("https://www.youtube.com/channel/UCUaQMQS9lY5lit3vurpXQ6w");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor.getServiceId());
        }

        @Test
        public void testName() throws Exception {
            assertEquals("random channel", extractor.getName());
        }

        @Test
        public void testId() throws Exception {
            assertEquals("UCUaQMQS9lY5lit3vurpXQ6w", extractor.getId());
        }

        @Test
        public void testCleanUrl() {
            assertEquals("https://www.youtube.com/channel/UCUaQMQS9lY5lit3vurpXQ6w", extractor.getCleanUrl());
        }

        @Test
        public void testOriginalUrl() {
            assertEquals("https://www.youtube.com/channel/UCUaQMQS9lY5lit3vurpXQ6w", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor, YouTube.getServiceId());
        }

        @Test
        public void testMoreRelatedItems() {
            try {
                defaultTestMoreItems(extractor, YouTube.getServiceId());
            } catch (Throwable ignored) {
                return;
            }

            fail("This channel doesn't have more items, it should throw an error");
        }

         /*//////////////////////////////////////////////////////////////////////////
         // ChannelExtractor
         //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testDescription() throws Exception {
            final String description = extractor.getDescription();
            assertTrue(description, description.contains("Hey there iu will upoload a load of pranks onto this channel"));
        }

        @Test
        public void testAvatarUrl() throws Exception {
            String avatarUrl = extractor.getAvatarUrl();
            assertIsSecureUrl(avatarUrl);
            assertTrue(avatarUrl, avatarUrl.contains("yt3"));
        }

        @Test
        public void testBannerUrl() throws Exception {
            String bannerUrl = extractor.getBannerUrl();
            assertIsSecureUrl(bannerUrl);
            assertTrue(bannerUrl, bannerUrl.contains("yt3"));
        }

        @Test
        public void testFeedUrl() throws Exception {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCUaQMQS9lY5lit3vurpXQ6w", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() throws Exception {
            assertTrue("Wrong subscriber count", extractor.getSubscriberCount() >= 50);
        }

        @Test
        public void testChannelDonation() throws Exception {
            assertTrue(extractor.getDonationLinks().length == 0);
        }
    }
};

