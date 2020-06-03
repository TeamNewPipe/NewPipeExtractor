package org.schabi.newpipe.extractor.services.peertube;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelExtractor;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.*;

/**
 * Test for {@link PeertubeChannelExtractor}
 */
public class PeertubeChannelExtractorTest {
    public static class DanDAugeTutoriels implements BaseChannelExtractorTest {
        private static PeertubeChannelExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://peertube.mastodon.host", "PeerTube on Mastodon.host"));
            extractor = (PeertubeChannelExtractor) PeerTube
                    .getChannelExtractor("https://peertube.mastodon.host/api/v1/video-channels/7682d9f2-07be-4622-862e-93ec812e2ffa");
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
            assertEquals("Dan d'Auge tutoriels", extractor.getName());
        }

        @Test
        public void testId() throws ParsingException {
            assertEquals("video-channels/7682d9f2-07be-4622-862e-93ec812e2ffa", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://peertube.mastodon.host/api/v1/video-channels/7682d9f2-07be-4622-862e-93ec812e2ffa", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://peertube.mastodon.host/video-channels/7682d9f2-07be-4622-862e-93ec812e2ffa", extractor.getOriginalUrl());
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
        public void testDescription() throws ParsingException {
            assertNotNull(extractor.getDescription());
        }

        @Test
        public void testParentChannelName() throws ParsingException {
            assertEquals("libux", extractor.getParentChannelName());
        }

        @Test
        public void testParentChannelUrl() throws ParsingException {
            assertEquals("https://peertube.mastodon.host/accounts/libux", extractor.getParentChannelUrl());
        }

        @Test
        public void testParentChannelAvatarUrl() throws ParsingException {
            assertIsSecureUrl(extractor.getParentChannelAvatarUrl());
        }

        @Test
        public void testAvatarUrl() throws ParsingException {
            assertIsSecureUrl(extractor.getAvatarUrl());
        }

        @Ignore
        @Test
        public void testBannerUrl() throws ParsingException {
            assertIsSecureUrl(extractor.getBannerUrl());
        }

        @Test
        public void testFeedUrl() throws ParsingException {
            assertEquals("https://peertube.mastodon.host/feeds/videos.xml?videoChannelId=1361", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() throws ParsingException {
            assertTrue("Wrong subscriber count", extractor.getSubscriberCount() >= 4);
        }
    }

    public static class Divers implements BaseChannelExtractorTest {
        private static PeertubeChannelExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel
            PeerTube.setInstance(new PeertubeInstance("https://peertube.mastodon.host", "PeerTube on Mastodon.host"));
            extractor = (PeertubeChannelExtractor) PeerTube
                    .getChannelExtractor("https://peertube.mastodon.host/video-channels/35080089-79b6-45fc-96ac-37e4d46a4457");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Additional Testing
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testGetPageInNewExtractor() throws Exception {
            final ChannelExtractor newExtractor = PeerTube.getChannelExtractor(extractor.getUrl());
            defaultTestGetPageInNewExtractor(extractor, newExtractor);
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
            assertEquals("Divers", extractor.getName());
        }

        @Test
        public void testId() throws ParsingException {
            assertEquals("video-channels/35080089-79b6-45fc-96ac-37e4d46a4457", extractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://peertube.mastodon.host/api/v1/video-channels/35080089-79b6-45fc-96ac-37e4d46a4457", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://peertube.mastodon.host/video-channels/35080089-79b6-45fc-96ac-37e4d46a4457", extractor.getOriginalUrl());
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
        public void testDescription() throws ParsingException {
            assertNotNull(extractor.getDescription());
        }

        @Test
        public void testParentChannelName() throws ParsingException {
            assertEquals("booteille", extractor.getParentChannelName());
        }

        @Test
        public void testParentChannelUrl() throws ParsingException {
            assertEquals("https://peertube.mastodon.host/accounts/booteille", extractor.getParentChannelUrl());
        }

        @Test
        public void testParentChannelAvatarUrl() throws ParsingException {
            assertIsSecureUrl(extractor.getParentChannelAvatarUrl());
        }

        @Test
        public void testAvatarUrl() throws ParsingException {
            assertIsSecureUrl(extractor.getAvatarUrl());
        }

        @Ignore
        @Test
        public void testBannerUrl() throws ParsingException {
            assertIsSecureUrl(extractor.getBannerUrl());
        }

        @Test
        public void testFeedUrl() throws ParsingException {
            assertEquals("https://peertube.mastodon.host/feeds/videos.xml?videoChannelId=1227", extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() throws ParsingException {
            assertTrue("Wrong subscriber count", extractor.getSubscriberCount() >= 2);
        }
    }
}
