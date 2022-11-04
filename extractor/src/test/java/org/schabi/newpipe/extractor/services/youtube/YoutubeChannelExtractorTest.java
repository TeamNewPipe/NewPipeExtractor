package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertContains;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestGetPageInNewExtractor;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestMoreItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.AccountTerminatedException;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelExtractor;

import java.io.IOException;

/**
 * Test for {@link ChannelExtractor}
 */
public class YoutubeChannelExtractorTest {

    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/channel/";

    public static class NotAvailable {
        @BeforeAll
        public static void setUp() throws IOException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "notAvailable"));
        }

        @Test
        public void deletedFetch() throws Exception {
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://www.youtube.com/channel/UCAUc4iz6edWerIjlnL8OSSw");

            assertThrows(ContentNotAvailableException.class, extractor::fetchPage);
        }

        @Test
        public void nonExistentFetch() throws Exception {
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://www.youtube.com/channel/DOESNT-EXIST");

            assertThrows(ContentNotAvailableException.class, extractor::fetchPage);
        }

        @Test
        public void accountTerminatedTOSFetch() throws Exception {
            // "This account has been terminated for a violation of YouTube's Terms of Service."
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://www.youtube.com/channel/UCTGjY2I-ZUGnwVoWAGRd7XQ");

            AccountTerminatedException ex =
                    assertThrows(AccountTerminatedException.class, extractor::fetchPage);
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.getReason());
        }

        @Test
        public void accountTerminatedCommunityFetch() throws Exception {
            // "This account has been terminated for violating YouTube's Community Guidelines."
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://www.youtube.com/channel/UC0AuOxCr9TZ0TtEgL1zpIgA");

            AccountTerminatedException ex =
                    assertThrows(AccountTerminatedException.class, extractor::fetchPage);
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.getReason());
        }

        @Test
        public void accountTerminatedHateFetch() throws Exception {
            // "This account has been terminated due to multiple or severe violations
            // of YouTube's policy prohibiting hate speech."
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://www.youtube.com/channel/UCPWXIOPK-9myzek6jHR5yrg");

            AccountTerminatedException ex =
                    assertThrows(AccountTerminatedException.class, extractor::fetchPage);
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.getReason());
        }

        @Test
        public void accountTerminatedBullyFetch() throws Exception {
            // "This account has been terminated due to multiple or severe violations
            // of YouTube's policy prohibiting content designed to harass, bully or threaten."
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://youtube.com/channel/UCB1o7_gbFp2PLsamWxFenBg");

            AccountTerminatedException ex =
                    assertThrows(AccountTerminatedException.class, extractor::fetchPage);
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.getReason());
        }

        @Test
        public void accountTerminatedSpamFetch() throws Exception {
            // "This account has been terminated due to multiple or severe violations
            // of YouTube's policy against spam, deceptive practices and misleading content
            // or other Terms of Service violations."
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://www.youtube.com/channel/UCoaO4U_p7G7AwalqSbGCZOA");

            AccountTerminatedException ex =
                    assertThrows(AccountTerminatedException.class, extractor::fetchPage);
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.getReason());
        }

        @Test
        public void accountTerminatedCopyrightFetch() throws Exception {
            // "This account has been terminated because we received multiple third-party claims
            // of copyright infringement regarding material that the user posted."
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://www.youtube.com/channel/UCI4i4RgFT5ilfMpna4Z_Y8w");

            AccountTerminatedException ex =
                    assertThrows(AccountTerminatedException.class, extractor::fetchPage);
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.getReason());
        }

    }

    public static class NotSupported {
        @BeforeAll
        public static void setUp() throws IOException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "notSupported"));
        }

        @Test
        void noVideoTab() throws Exception {
            final ChannelExtractor extractor = YouTube.getChannelExtractor("https://invidio.us/channel/UC-9-kyTW8ZkZNDHQJ6FgpwQ");

            extractor.fetchPage();
            assertThrows(ContentNotSupportedException.class, extractor::getInitialPage);
        }
    }

    public static class Gronkh implements BaseChannelExtractorTest {
        private static YoutubeChannelExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "gronkh"));
            extractor = (YoutubeChannelExtractor) YouTube
                    .getChannelExtractor("http://www.youtube.com/@Gronkh");
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
            assertEquals("http://www.youtube.com/@Gronkh", extractor.getOriginalUrl());
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
            assertContains("Ungebremster Spieltrieb seit 1896.", extractor.getDescription());
        }

        @Test
        public void testAvatarUrl() throws Exception {
            String avatarUrl = extractor.getAvatarUrl();
            assertIsSecureUrl(avatarUrl);
            ExtractorAsserts.assertContains("yt3", avatarUrl);
        }

        @Test
        public void testBannerUrl() throws Exception {
            String bannerUrl = extractor.getBannerUrl();
            assertIsSecureUrl(bannerUrl);
            ExtractorAsserts.assertContains("yt3", bannerUrl);
        }

        @Test
        public void testFeedUrl() throws Exception {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCYJ61XIK64sp6ZFFS8sctxw", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() throws Exception {
            ExtractorAsserts.assertGreaterOrEqual(4_900_000, extractor.getSubscriberCount());
        }

        @Override
        public void testVerified() throws Exception {
            assertTrue(extractor.isVerified());
        }

    }

    // Youtube RED/Premium ad blocking test
    public static class VSauce implements BaseChannelExtractorTest {
        private static YoutubeChannelExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "VSauce"));
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
            assertContains("Our World is Amazing. \n\nQuestions? Ideas? Tweet me:", extractor.getDescription());
        }

        @Test
        public void testAvatarUrl() throws Exception {
            String avatarUrl = extractor.getAvatarUrl();
            assertIsSecureUrl(avatarUrl);
            ExtractorAsserts.assertContains("yt3", avatarUrl);
        }

        @Test
        public void testBannerUrl() throws Exception {
            String bannerUrl = extractor.getBannerUrl();
            assertIsSecureUrl(bannerUrl);
            ExtractorAsserts.assertContains("yt3", bannerUrl);
        }

        @Test
        public void testFeedUrl() throws Exception {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UC6nSFpj9HTCZ5t-N3Rm3-HA", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() throws Exception {
            ExtractorAsserts.assertGreaterOrEqual(17_000_000, extractor.getSubscriberCount());
        }

        @Test
        public void testVerified() throws Exception {
            assertTrue(extractor.isVerified());
        }

    }

    public static class Kurzgesagt implements BaseChannelExtractorTest {
        private static YoutubeChannelExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "kurzgesagt"));
            extractor = (YoutubeChannelExtractor) YouTube
                    .getChannelExtractor("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q");
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
            assertTrue(extractor.getName().startsWith("Kurzgesagt"));
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
            ExtractorAsserts.assertContains("science", extractor.getDescription());
            ExtractorAsserts.assertContains("animators", extractor.getDescription());
            //TODO: Description get cuts out, because the og:description is optimized and don't have all the content
            //assertTrue(description, description.contains("Currently we make one animation video per month"));
        }

        @Test
        public void testAvatarUrl() throws Exception {
            String avatarUrl = extractor.getAvatarUrl();
            assertIsSecureUrl(avatarUrl);
            ExtractorAsserts.assertContains("yt3", avatarUrl);
        }

        @Test
        public void testBannerUrl() throws Exception {
            String bannerUrl = extractor.getBannerUrl();
            assertIsSecureUrl(bannerUrl);
            ExtractorAsserts.assertContains("yt3", bannerUrl);
        }

        @Test
        public void testFeedUrl() throws Exception {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCsXVk37bltHxD1rDPwtNM8Q", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() throws Exception {
            ExtractorAsserts.assertGreaterOrEqual(17_000_000, extractor.getSubscriberCount());
        }

        @Test
        public void testVerified() throws Exception {
            assertTrue(extractor.isVerified());
        }
    }

    public static class KurzgesagtAdditional {

        private static YoutubeChannelExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            // Test is not deterministic, mocks can't be used
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeChannelExtractor) YouTube
                    .getChannelExtractor("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q");
            extractor.fetchPage();
        }

        @Test
        public void testGetPageInNewExtractor() throws Exception {
            final ChannelExtractor newExtractor = YouTube.getChannelExtractor(extractor.getUrl());
            defaultTestGetPageInNewExtractor(extractor, newExtractor);
        }
    }

    public static class CaptainDisillusion implements BaseChannelExtractorTest {
        private static YoutubeChannelExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "captainDisillusion"));
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
            ExtractorAsserts.assertContains("In a world where", extractor.getDescription());
        }

        @Test
        public void testAvatarUrl() throws Exception {
            String avatarUrl = extractor.getAvatarUrl();
            assertIsSecureUrl(avatarUrl);
            ExtractorAsserts.assertContains("yt3", avatarUrl);
        }

        @Test
        public void testBannerUrl() throws Exception {
            String bannerUrl = extractor.getBannerUrl();
            assertIsSecureUrl(bannerUrl);
            ExtractorAsserts.assertContains("yt3", bannerUrl);
        }

        @Test
        public void testFeedUrl() throws Exception {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCEOXxzW2vU0P-0THehuIIeg", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() throws Exception {
            ExtractorAsserts.assertGreaterOrEqual(2_000_000, extractor.getSubscriberCount());
        }

        @Test
        public void testVerified() throws Exception {
            assertTrue(extractor.isVerified());
        }
    }

    public static class RandomChannel implements BaseChannelExtractorTest {
        private static YoutubeChannelExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "random"));
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
            } catch (final Throwable ignored) {
                return;
            }

            fail("This channel doesn't have more items, it should throw an error");
        }

         /*//////////////////////////////////////////////////////////////////////////
         // ChannelExtractor
         //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testDescription() throws Exception {
            ExtractorAsserts.assertContains("Hey there iu will upoload a load of pranks onto this channel", extractor.getDescription());
        }

        @Test
        public void testAvatarUrl() throws Exception {
            String avatarUrl = extractor.getAvatarUrl();
            assertIsSecureUrl(avatarUrl);
            ExtractorAsserts.assertContains("yt3", avatarUrl);
        }

        @Test
        public void testBannerUrl() throws Exception {
            String bannerUrl = extractor.getBannerUrl();
            assertIsSecureUrl(bannerUrl);
            ExtractorAsserts.assertContains("yt3", bannerUrl);
        }

        @Test
        public void testFeedUrl() throws Exception {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCUaQMQS9lY5lit3vurpXQ6w", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() throws Exception {
            ExtractorAsserts.assertGreaterOrEqual(50, extractor.getSubscriberCount());
        }

        @Test
        public void testVerified() throws Exception {
            assertFalse(extractor.isVerified());
        }
    }
}
