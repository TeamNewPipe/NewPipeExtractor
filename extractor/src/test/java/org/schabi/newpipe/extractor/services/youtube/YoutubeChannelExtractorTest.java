package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelExtractor;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmpty;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.*;

/**
 * Test for {@link ChannelExtractor}
 */
public class YoutubeChannelExtractorTest {

    public static class NotAvailable {
        @BeforeClass
        public static void setUp() {
            NewPipe.init(DownloaderTestImpl.getInstance());
        }

        @Test(expected = ContentNotAvailableException.class)
        public void deletedFetch() throws Exception {
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://www.youtube.com/channel/UCAUc4iz6edWerIjlnL8OSSw");
            extractor.fetchPage();
        }

        @Test(expected = ContentNotAvailableException.class)
        public void nonExistentFetch() throws Exception {
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://www.youtube.com/channel/DOESNT-EXIST");
            extractor.fetchPage();
        }
    }

    public static class NotSupported {
        @BeforeClass
        public static void setUp() {
            NewPipe.init(DownloaderTestImpl.getInstance());
        }

        @Test(expected = ContentNotSupportedException.class)
        public void noVideoTab() throws Exception {
            final ChannelExtractor extractor = YouTube.getChannelExtractor("https://invidio.us/channel/UC-9-kyTW8ZkZNDHQJ6FgpwQ");
            extractor.fetchPage();
            extractor.getInitialPage();
        }
    }

    public static class Gronkh implements BaseChannelExtractorTest {
        private static YoutubeChannelExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
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
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCYJ61XIK64sp6ZFFS8sctxw", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("http://www.youtube.com/user/Gronkh", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
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
            assertTrue("Subscriber count too small", extractor.getSubscriberCount() >= 4e6);
        }
    }

    // Youtube RED/Premium ad blocking test
    public static class VSauce implements BaseChannelExtractorTest {
        private static YoutubeChannelExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeChannelExtractor) YouTube
                    .getChannelExtractor("https://www.youtube.com/user/Vsauce");
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
            assertEquals("Vsauce", extractor.getName());
        }

        @Test
        public void testId() throws Exception {
            assertEquals("UC6nSFpj9HTCZ5t-N3Rm3-HA", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UC6nSFpj9HTCZ5t-N3Rm3-HA", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/user/Vsauce", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }

         /*//////////////////////////////////////////////////////////////////////////
         // ChannelExtractor
         //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testDescription() throws Exception {
            assertTrue("What it actually was: " + extractor.getDescription(),
                    extractor.getDescription().contains("Our World is Amazing. \n\nQuestions? Ideas? Tweet me:"));
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
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UC6nSFpj9HTCZ5t-N3Rm3-HA", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() throws Exception {
            assertTrue("Wrong subscriber count", extractor.getSubscriberCount() >= 0);
            assertTrue("Subscriber count too small", extractor.getSubscriberCount() >= 10e6);
        }

    }

    public static class Kurzgesagt implements BaseChannelExtractorTest {
        private static YoutubeChannelExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeChannelExtractor) YouTube
                    .getChannelExtractor("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Additional Testing
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testGetPageInNewExtractor() throws Exception {
            final ChannelExtractor newExtractor = YouTube.getChannelExtractor(extractor.getUrl());
            defaultTestGetPageInNewExtractor(extractor, newExtractor);
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
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
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
    }

    public static class CaptainDisillusion implements BaseChannelExtractorTest {
        private static YoutubeChannelExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
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
            assertEquals("Captain Disillusion", extractor.getName());
        }

        @Test
        public void testId() throws Exception {
            assertEquals("UCEOXxzW2vU0P-0THehuIIeg", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCEOXxzW2vU0P-0THehuIIeg", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/user/CaptainDisillusion/videos", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
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

    // this channel has no "Subscribe" button
    public static class EminemVEVO implements BaseChannelExtractorTest {
        private static YoutubeChannelExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeChannelExtractor) YouTube
                    .getChannelExtractor("https://www.youtube.com/user/EminemVEVO/");
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
            assertEquals("EminemVEVO", extractor.getName());
        }

        @Test
        public void testId() throws Exception {
            assertEquals("UC20vb-R_px4CguHzzBPhoyQ", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UC20vb-R_px4CguHzzBPhoyQ", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/user/EminemVEVO/", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }

         /*//////////////////////////////////////////////////////////////////////////
         // ChannelExtractor
         //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testDescription() throws Exception {
            final String description = extractor.getDescription();
            assertTrue(description, description.contains("Eminem on Vevo"));
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
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UC20vb-R_px4CguHzzBPhoyQ", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() throws Exception {
            // there is no "Subscribe" button
            long subscribers = extractor.getSubscriberCount();
            assertEquals("Wrong subscriber count", -1, subscribers);
        }
    }

    /**
     * Some VEVO channels will redirect to a new page with a new channel id.
     * <p>
     * Though, it isn't a simple redirect, but a redirect instruction embed in the response itself, this
     * test assure that we account for that.
     */
    public static class RedirectedChannel implements BaseChannelExtractorTest {
        private static YoutubeChannelExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeChannelExtractor) YouTube
                    .getChannelExtractor("https://www.youtube.com/channel/UCITk7Ky4iE5_xISw9IaHqpQ");
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
            assertEquals("LordiVEVO", extractor.getName());
        }

        @Test
        public void testId() throws Exception {
            assertEquals("UCrxkwepj7-4Wz1wHyfzw-sQ", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCrxkwepj7-4Wz1wHyfzw-sQ", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCITk7Ky4iE5_xISw9IaHqpQ", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            assertNoMoreItems(extractor);
        }

         /*//////////////////////////////////////////////////////////////////////////
         // ChannelExtractor
         //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testDescription() throws Exception {
            assertEmpty(extractor.getDescription());
        }

        @Test
        public void testAvatarUrl() throws Exception {
            String avatarUrl = extractor.getAvatarUrl();
            assertIsSecureUrl(avatarUrl);
            assertTrue(avatarUrl, avatarUrl.contains("yt3"));
        }

        @Test
        public void testBannerUrl() throws Exception {
            assertEmpty(extractor.getBannerUrl());
        }

        @Test
        public void testFeedUrl() throws Exception {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCrxkwepj7-4Wz1wHyfzw-sQ", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() throws Exception {
            assertEquals(-1, extractor.getSubscriberCount());
        }
    }

    public static class RandomChannel implements BaseChannelExtractorTest {
        private static YoutubeChannelExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
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
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCUaQMQS9lY5lit3vurpXQ6w", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCUaQMQS9lY5lit3vurpXQ6w", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        public void testMoreRelatedItems() {
            try {
                defaultTestMoreItems(extractor);
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
            long subscribers = extractor.getSubscriberCount();
            assertTrue("Wrong subscriber count: " + subscribers, subscribers >= 50);
        }
    }
}

