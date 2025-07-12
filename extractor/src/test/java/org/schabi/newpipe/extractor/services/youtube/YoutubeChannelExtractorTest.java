package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertContains;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmpty;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertNotEmpty;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertTabsContain;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestGetPageInNewExtractor;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.InitNewPipeTest;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.AccountTerminatedException;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler;
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelTabPlaylistExtractor;

import java.util.List;

/**
 * Test for {@link ChannelExtractor}
 */
public class YoutubeChannelExtractorTest {

    public static class NotAvailable implements InitYoutubeTest {
        @Test
        void deletedFetch() throws Exception {
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://www.youtube.com/channel/UCAUc4iz6edWerIjlnL8OSSw");

            assertThrows(ContentNotAvailableException.class, extractor::fetchPage);
        }

        @Test
        void nonExistentFetch() throws Exception {
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://www.youtube.com/channel/DOESNT-EXIST");

            assertThrows(ContentNotAvailableException.class, extractor::fetchPage);
        }

        @Test
        void accountTerminatedTOSFetch() throws Exception {
            // "This account has been terminated for a violation of YouTube's Terms of Service."
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://www.youtube.com/channel/UCTGjY2I-ZUGnwVoWAGRd7XQ");

            final AccountTerminatedException ex =
                    assertThrows(AccountTerminatedException.class, extractor::fetchPage);
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.getReason());
        }

        @Test
        void accountTerminatedCommunityFetch() throws Exception {
            // "This account has been terminated for violating YouTube's Community Guidelines."
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://www.youtube.com/channel/UC0AuOxCr9TZ0TtEgL1zpIgA");

            final AccountTerminatedException ex =
                    assertThrows(AccountTerminatedException.class, extractor::fetchPage);
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.getReason());
        }

        @Test
        void accountTerminatedHateFetch() throws Exception {
            // "This account has been terminated due to multiple or severe violations
            // of YouTube's policy prohibiting hate speech."
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://www.youtube.com/channel/UCPWXIOPK-9myzek6jHR5yrg");

            final AccountTerminatedException ex =
                    assertThrows(AccountTerminatedException.class, extractor::fetchPage);
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.getReason());
        }

        @Test
        void accountTerminatedBullyFetch() throws Exception {
            // "This account has been terminated due to multiple or severe violations
            // of YouTube's policy prohibiting content designed to harass, bully or threaten."
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://youtube.com/channel/UCB1o7_gbFp2PLsamWxFenBg");

            final AccountTerminatedException ex =
                    assertThrows(AccountTerminatedException.class, extractor::fetchPage);
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.getReason());
        }

        @Test
        void accountTerminatedSpamFetch() throws Exception {
            // "This account has been terminated due to multiple or severe violations
            // of YouTube's policy against spam, deceptive practices and misleading content
            // or other Terms of Service violations."
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://www.youtube.com/channel/UCoaO4U_p7G7AwalqSbGCZOA");

            final AccountTerminatedException ex =
                    assertThrows(AccountTerminatedException.class, extractor::fetchPage);
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.getReason());
        }

        @Test
        void accountTerminatedCopyrightFetch() throws Exception {
            // "This account has been terminated because we received multiple third-party claims
            // of copyright infringement regarding material that the user posted."
            final ChannelExtractor extractor =
                    YouTube.getChannelExtractor("https://www.youtube.com/channel/UCI4i4RgFT5ilfMpna4Z_Y8w");

            final AccountTerminatedException ex =
                    assertThrows(AccountTerminatedException.class, extractor::fetchPage);
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.getReason());
        }

    }

    static class SystemTopic implements InitYoutubeTest {
        @Test
        void noSupportedTab() throws Exception {
            final ChannelExtractor extractor = YouTube.getChannelExtractor("https://invidio.us/channel/UC-9-kyTW8ZkZNDHQJ6FgpwQ");

            extractor.fetchPage();
            assertTrue(extractor.getTabs().isEmpty());
        }
    }

    abstract static class Base extends DefaultSimpleExtractorTest<YoutubeChannelExtractor>
        implements BaseChannelExtractorTest, InitYoutubeTest {

        @Override
        protected YoutubeChannelExtractor createExtractor() throws Exception {
            return (YoutubeChannelExtractor) YouTube.getChannelExtractor(urlForExtractor());
        }

        protected abstract String urlForExtractor();
    }

    public static class Gronkh extends Base {

        @Override
        protected String urlForExtractor() {
            return "http://www.youtube.com/@Gronkh";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws Exception {
            assertEquals("Gronkh", extractor().getName());
        }

        @Override
        @Test
        public void testId() throws Exception {
            assertEquals("UCYJ61XIK64sp6ZFFS8sctxw", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCYJ61XIK64sp6ZFFS8sctxw", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("http://www.youtube.com/@Gronkh", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testDescription() throws Exception {
            assertContains("Ungebremster Spieltrieb seit 1896.", extractor().getDescription());
        }

        @Override
        @Test
        public void testAvatars() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getAvatars());
        }

        @Override
        @Test
        public void testBanners() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getBanners());
        }

        @Override
        @Test
        public void testFeedUrl() throws Exception {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCYJ61XIK64sp6ZFFS8sctxw", extractor().getFeedUrl());
        }

        @Override
        @Test
        public void testSubscriberCount() throws Exception {
            ExtractorAsserts.assertGreaterOrEqual(4_900_000, extractor().getSubscriberCount());
        }

        @Test
        @Override
        public void testVerified() throws Exception {
            assertTrue(extractor().isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            assertTabsContain(extractor().getTabs(), ChannelTabs.VIDEOS, ChannelTabs.PLAYLISTS);
            assertTrue(extractor().getTabs().stream()
                    .filter(it -> ChannelTabs.VIDEOS.equals(it.getContentFilters().get(0)))
                    .allMatch(ReadyChannelTabListLinkHandler.class::isInstance));
        }

        @Test
        @Override
        public void testTags() throws Exception {
            assertTrue(extractor().getTags().contains("gronkh"));
        }
    }

    // YouTube RED/Premium ad blocking test
    public static class VSauce extends Base {
        @Override
        protected String urlForExtractor() {
            return "https://www.youtube.com/user/Vsauce";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws Exception {
            assertEquals("Vsauce", extractor().getName());
        }

        @Override
        @Test
        public void testId() throws Exception {
            assertEquals("UC6nSFpj9HTCZ5t-N3Rm3-HA", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UC6nSFpj9HTCZ5t-N3Rm3-HA", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/user/Vsauce", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testDescription() throws Exception {
            assertContains("Our World is Amazing", extractor().getDescription());
        }

        @Override
        @Test
        public void testAvatars() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getAvatars());
        }

        @Override
        @Test
        public void testBanners() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getBanners());
        }

        @Override
        @Test
        public void testFeedUrl() throws Exception {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UC6nSFpj9HTCZ5t-N3Rm3-HA", extractor().getFeedUrl());
        }

        @Override
        @Test
        public void testSubscriberCount() throws Exception {
            ExtractorAsserts.assertGreaterOrEqual(17_000_000, extractor().getSubscriberCount());
        }

        @Override
        @Test
        public void testVerified() throws Exception {
            assertTrue(extractor().isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            assertTabsContain(extractor().getTabs(), ChannelTabs.VIDEOS, ChannelTabs.LIVESTREAMS,
                    ChannelTabs.SHORTS, ChannelTabs.PLAYLISTS);
            assertTrue(extractor().getTabs().stream()
                    .filter(it -> ChannelTabs.VIDEOS.equals(it.getContentFilters().get(0)))
                    .allMatch(ReadyChannelTabListLinkHandler.class::isInstance));
        }

        @Test
        @Override
        public void testTags() throws Exception {
            assertTrue(extractor().getTags().containsAll(List.of("questions", "education",
                    "learning", "schools", "Science")));
        }
    }

    public static class Kurzgesagt extends Base {
        @Override
        protected String urlForExtractor() {
            return "https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws Exception {
            assertTrue(extractor().getName().startsWith("Kurzgesagt"));
        }

        @Override
        @Test
        public void testId() throws Exception {
            assertEquals("UCsXVk37bltHxD1rDPwtNM8Q", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testDescription() throws Exception {
            ExtractorAsserts.assertContains("science", extractor().getDescription());
            ExtractorAsserts.assertContains("animators", extractor().getDescription());
            //TODO: Description get cuts out, because the og:description is optimized and don't have all the content
            //assertTrue(description, description.contains("Currently we make one animation video per month"));
        }

        @Override
        @Test
        public void testAvatars() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getAvatars());
        }

        @Override
        @Test
        public void testBanners() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getBanners());
        }

        @Override
        @Test
        public void testFeedUrl() throws Exception {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCsXVk37bltHxD1rDPwtNM8Q", extractor().getFeedUrl());
        }

        @Override
        @Test
        public void testSubscriberCount() throws Exception {
            ExtractorAsserts.assertGreaterOrEqual(17_000_000, extractor().getSubscriberCount());
        }

        @Override
        @Test
        public void testVerified() throws Exception {
            assertTrue(extractor().isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            assertTabsContain(extractor().getTabs(), ChannelTabs.VIDEOS, ChannelTabs.SHORTS,
                    ChannelTabs.PLAYLISTS);
            assertTrue(extractor().getTabs().stream()
                    .filter(it -> ChannelTabs.VIDEOS.equals(it.getContentFilters().get(0)))
                    .allMatch(ReadyChannelTabListLinkHandler.class::isInstance));
        }

        @Test
        @Override
        public void testTags() throws Exception {
            assertTrue(extractor().getTags().containsAll(List.of("universe", "Science",
                    "black hole", "humanism", "evolution")));
        }
    }

    public static class KurzgesagtAdditional extends DefaultSimpleExtractorTest<YoutubeChannelExtractor>
        implements InitYoutubeTest {

        private ChannelTabExtractor tabExtractor;

        @Override
        protected YoutubeChannelExtractor createExtractor() throws Exception {
            return (YoutubeChannelExtractor) YouTube.getChannelExtractor(
                "https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q");
        }

        @Override
        protected void fetchExtractor(final YoutubeChannelExtractor extractor) throws Exception {
            super.fetchExtractor(extractor);

            tabExtractor = YouTube.getChannelTabExtractor(extractor.getTabs().get(0));
            tabExtractor.fetchPage();
        }

        @Test
        void testGetPageInNewExtractor() throws Exception {
            extractor(); // Init
            // Init downloader again for mock as otherwise request confusion occurs when using Mock
            InitNewPipeTest.initNewPipe(this.getClass(), "testGetPageInNewExtractor");

            final ChannelExtractor newExtractor = YouTube.getChannelExtractor(extractor().getUrl());
            newExtractor.fetchPage();
            final ChannelTabExtractor newTabExtractor = YouTube.getChannelTabExtractor(
                    newExtractor.getTabs().get(0));
            defaultTestGetPageInNewExtractor(tabExtractor, newTabExtractor);
        }
    }

    public static class CaptainDisillusion extends Base {
        @Override
        protected String urlForExtractor() {
            return "https://www.youtube.com/user/CaptainDisillusion/videos";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws Exception {
            assertEquals("Captain Disillusion", extractor().getName());
        }

        @Override
        @Test
        public void testId() throws Exception {
            assertEquals("UCEOXxzW2vU0P-0THehuIIeg", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCEOXxzW2vU0P-0THehuIIeg", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/user/CaptainDisillusion/videos", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testDescription() throws Exception {
            assertContains("In a world where", extractor().getDescription());
        }

        @Override
        @Test
        public void testAvatars() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getAvatars());
        }

        @Override
        @Test
        public void testBanners() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getBanners());
        }

        @Override
        @Test
        public void testFeedUrl() throws Exception {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCEOXxzW2vU0P-0THehuIIeg", extractor().getFeedUrl());
        }

        @Override
        @Test
        public void testSubscriberCount() throws Exception {
            ExtractorAsserts.assertGreaterOrEqual(2_000_000, extractor().getSubscriberCount());
        }

        @Override
        @Test
        public void testVerified() throws Exception {
            assertTrue(extractor().isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            assertTabsContain(extractor().getTabs(),
                    ChannelTabs.VIDEOS, ChannelTabs.PLAYLISTS, ChannelTabs.SHORTS);
            assertTrue(extractor().getTabs().stream()
                    .filter(it -> ChannelTabs.VIDEOS.equals(it.getContentFilters().get(0)))
                    .allMatch(ReadyChannelTabListLinkHandler.class::isInstance));
        }

        @Test
        @Override
        public void testTags() throws Exception {
            assertTrue(extractor().getTags().containsAll(List.of("critical thinking",
                    "visual effects", "VFX", "sci-fi", "humor")));
        }
    }

    public static class RandomChannel extends Base {
        @Override
        protected String urlForExtractor() {
            return "https://www.youtube.com/channel/UCUaQMQS9lY5lit3vurpXQ6w";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws Exception {
            assertEquals("random channel", extractor().getName());
        }

        @Override
        @Test
        public void testId() throws Exception {
            assertEquals("UCUaQMQS9lY5lit3vurpXQ6w", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCUaQMQS9lY5lit3vurpXQ6w", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCUaQMQS9lY5lit3vurpXQ6w", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testDescription() throws Exception {
            assertContains("Hey there iu will upoload a load of pranks onto this channel", extractor().getDescription());
        }

        @Override
        @Test
        public void testAvatars() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getAvatars());
        }

        @Override
        @Test
        public void testBanners() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getBanners());
        }

        @Override
        @Test
        public void testFeedUrl() throws Exception {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCUaQMQS9lY5lit3vurpXQ6w", extractor().getFeedUrl());
        }

        @Override
        @Test
        public void testSubscriberCount() throws Exception {
            ExtractorAsserts.assertGreaterOrEqual(50, extractor().getSubscriberCount());
        }

        @Override
        @Test
        public void testVerified() throws Exception {
            assertFalse(extractor().isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            assertTabsContain(extractor().getTabs(), ChannelTabs.VIDEOS);
            assertTrue(extractor().getTabs().stream()
                    .filter(it -> ChannelTabs.VIDEOS.equals(it.getContentFilters().get(0)))
                    .allMatch(ReadyChannelTabListLinkHandler.class::isInstance));
        }

        @Test
        @Override
        public void testTags() throws Exception {
            assertTrue(extractor().getTags().isEmpty());
        }
    }

    public static class CarouselHeader extends Base {

        @Override
        protected String urlForExtractor() {
            return "https://www.youtube.com/channel/UCEgdi0XIXXZ-qJOFPf4JSKw";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws Exception {
            assertEquals("Sports", extractor().getName());
        }

        @Override
        @Test
        public void testId() throws Exception {
            assertEquals("UCEgdi0XIXXZ-qJOFPf4JSKw", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCEgdi0XIXXZ-qJOFPf4JSKw", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCEgdi0XIXXZ-qJOFPf4JSKw", extractor().getOriginalUrl());
        }

        @Test
        @Override
        public void testDescription() throws ParsingException {
            assertNull(extractor().getDescription());
        }

        @Override
        @Test
        public void testAvatars() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getAvatars());
        }

        @Override
        @Test
        public void testBanners() {
            // A CarouselHeaderRenderer doesn't contain a banner
            assertEmpty(extractor().getBanners());
        }

        @Override
        @Test
        public void testFeedUrl() throws Exception {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCEgdi0XIXXZ-qJOFPf4JSKw", extractor().getFeedUrl());
        }

        @Override
        @Test
        public void testSubscriberCount() throws Exception {
            ExtractorAsserts.assertGreaterOrEqual(70_000_000, extractor().getSubscriberCount());
        }

        @Override
        @Test
        public void testVerified() throws Exception {
            assertTrue(extractor().isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            assertEmpty(extractor().getTabs());
        }

        @Test
        @Override
        public void testTags() throws Exception {
            assertEmpty(extractor().getTags());
        }
    }

    /**
     * A YouTube channel which is age-restricted and requires login to view its contents on a
     * channel page.
     *
     * <p>
     * Note that age-restrictions on channels may not apply for countries, so check that the
     * channel is age-restricted in the network you use to update the test's mocks before updating
     * them.
     * </p>
     */
    static class AgeRestrictedChannel extends Base {

        @Override
        protected String urlForExtractor() {
            return "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig";
        }

        @Test
        @Override
        public void testDescription() throws Exception {
            // Description cannot be extracted from age-restricted channels
            assertTrue(isNullOrEmpty(extractor().getDescription()));
        }

        @Test
        @Override
        public void testAvatars() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getAvatars());
        }

        @Test
        @Override
        public void testBanners() throws Exception {
            // Banners cannot be extracted from age-restricted channels
            assertEmpty(extractor().getBanners());
        }

        @Test
        @Override
        public void testFeedUrl() throws Exception {
            assertEquals(
                    "https://www.youtube.com/feeds/videos.xml?channel_id=UCbfnHqxXs_K3kvaH-WlNlig",
                extractor().getFeedUrl());
        }

        @Test
        @Override
        public void testSubscriberCount() throws Exception {
            // Subscriber count cannot be extracted from age-restricted channels
            assertEquals(ChannelExtractor.UNKNOWN_SUBSCRIBER_COUNT, extractor().getSubscriberCount());
        }

        @Test
        @Override
        public void testServiceId() throws Exception {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Test
        @Override
        public void testName() throws Exception {
            assertEquals("Laphroaig Whisky", extractor().getName());
        }

        @Test
        @Override
        public void testId() throws Exception {
            assertEquals("UCbfnHqxXs_K3kvaH-WlNlig", extractor().getId());
        }

        @Test
        @Override
        public void testUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig",
                extractor().getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig",
                extractor().getOriginalUrl());
        }

        @Test
        @Override
        public void testVerified() throws Exception {
            // Verification status cannot be extracted from age-restricted channels
            assertFalse(extractor().isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            // Channel tabs which may be available and which will be extracted from channel system
            // uploads playlists
            assertTabsContain(extractor().getTabs(),
                    ChannelTabs.VIDEOS, ChannelTabs.SHORTS, ChannelTabs.LIVESTREAMS);

            // Check if all tabs are not classic tabs, so that link handlers are of the appropriate
            // type and build YoutubeChannelTabPlaylistExtractor instances
            assertTrue(extractor().getTabs()
                    .stream()
                    .allMatch(linkHandler ->
                            linkHandler.getClass() == ReadyChannelTabListLinkHandler.class
                    && ((ReadyChannelTabListLinkHandler) linkHandler)
                                .getChannelTabExtractor(extractor().getService())
                                    .getClass() == YoutubeChannelTabPlaylistExtractor.class));
        }

        @Test
        @Override
        public void testTags() throws Exception {
            // Tags cannot be extracted from age-restricted channels
            assertTrue(extractor().getTags().isEmpty());
        }
    }

    static class InteractiveTabbedHeader extends Base {

        @Override
        protected String urlForExtractor() {
            return "https://www.youtube.com/channel/UCQvWX73GQygcwXOTSf_VDVg";
        }

        @Test
        @Override
        public void testDescription() throws Exception {
            // The description changes frequently and there is no significant common word, so only
            // check if it is not empty
            assertNotEmpty(extractor().getDescription());
        }

        @Test
        @Override
        public void testAvatars() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getAvatars());
        }

        @Test
        @Override
        public void testBanners() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getBanners());
        }

        @Test
        @Override
        public void testFeedUrl() throws Exception {
            assertEquals(
                    "https://www.youtube.com/feeds/videos.xml?channel_id=UCQvWX73GQygcwXOTSf_VDVg",
                extractor().getFeedUrl());
        }

        @Test
        @Override
        public void testSubscriberCount() throws Exception {
            // Subscriber count is not available on channels with an interactiveTabbedHeaderRenderer
            assertEquals(ChannelExtractor.UNKNOWN_SUBSCRIBER_COUNT, extractor().getSubscriberCount());
        }

        @Test
        @Override
        public void testVerified() throws Exception {
            assertTrue(extractor().isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            // Gaming topic channels tabs are not yet supported
            // However, a Shorts tab like on other channel types is returned, so it is supported
            // Check that it is returned
            assertTabsContain(extractor().getTabs(), ChannelTabs.SHORTS);
        }

        @Test
        @Override
        public void testTags() throws Exception {
            assertTrue(extractor().getTags().isEmpty());
        }

        @Test
        @Override
        public void testServiceId() throws Exception {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Test
        @Override
        public void testName() throws Exception {
            assertContains("Minecraft", extractor().getName());
        }

        @Test
        @Override
        public void testId() throws Exception {
            assertEquals("UCQvWX73GQygcwXOTSf_VDVg", extractor().getId());
        }

        @Test
        @Override
        public void testUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UCQvWX73GQygcwXOTSf_VDVg",
                extractor().getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws Exception {
            assertEquals("https://www.youtube.com/channel/UCQvWX73GQygcwXOTSf_VDVg",
                extractor().getOriginalUrl());
        }
    }

    static class ChannelWithPronouns extends Base {

        @Override
        protected String urlForExtractor() {
            return "https://www.youtube.com/@ShempOfficial";
        }

        @Override
        @Test
        public void testServiceId() {
            assertEquals(YouTube.getServiceId(), extractor().getServiceId());
        }

        @Override
        @Test
        public void testName() throws Exception {
            assertEquals("Shemp", extractor().getName());
        }

        @Override
        @Test
        public void testId() throws Exception {
            assertEquals("UCEAXWzgcuF6XEiJcszhikQA", extractor().getId());
        }

        @Override
        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/channel/UCEAXWzgcuF6XEiJcszhikQA", extractor().getUrl());
        }

        @Override
        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/@ShempOfficial", extractor().getOriginalUrl());
        }

        @Override
        @Test
        public void testDescription() throws Exception {
            assertContains("HEY! HEY YOU! YEAH YOU!", extractor().getDescription());
        }

        @Override
        @Test
        public void testAvatars() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getAvatars());
        }

        @Override
        @Test
        public void testBanners() throws Exception {
            YoutubeTestsUtils.testImages(extractor().getBanners());
        }

        @Override
        @Test
        public void testFeedUrl() throws Exception {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCEAXWzgcuF6XEiJcszhikQA", extractor().getFeedUrl());
        }

        @Override
        @Test
        public void testSubscriberCount() throws Exception {
            ExtractorAsserts.assertGreaterOrEqual(7_000, extractor().getSubscriberCount());
        }

        @Override
        @Test
        public void testVerified() throws Exception {
            assertFalse(extractor().isVerified());
        }

        @Override
        @Test
        public void testTabs() throws Exception {
            assertTabsContain(extractor().getTabs(),
                    ChannelTabs.VIDEOS, ChannelTabs.LIVESTREAMS, ChannelTabs.PLAYLISTS);
            assertTrue(extractor().getTabs().stream()
                    .filter(it -> ChannelTabs.VIDEOS.equals(it.getContentFilters().get(0)))
                    .allMatch(ReadyChannelTabListLinkHandler.class::isInstance));
        }

        @Override
        @Test
        public void testTags() throws Exception {
            assertTrue(extractor().getTags().contains("shemp"));
        }
    }
}
