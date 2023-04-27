package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest;
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudChannelExtractor;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmpty;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.services.DefaultTests.*;

/**
 * Test for {@link SoundcloudChannelExtractor}
 */
public class SoundcloudChannelExtractorTest {
    public static class LilUzi implements BaseChannelExtractorTest {
        private static SoundcloudChannelExtractor extractor;
        private static ChannelTabExtractor tabExtractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (SoundcloudChannelExtractor) SoundCloud
                    .getChannelExtractor("http://soundcloud.com/liluzivert/sets");
            extractor.fetchPage();

            tabExtractor = SoundCloud.getChannelTabExtractor(extractor.getTabs().get(0));
            tabExtractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testServiceId() {
            assertEquals(SoundCloud.getServiceId(), extractor.getServiceId());
            assertEquals(SoundCloud.getServiceId(), tabExtractor.getServiceId());
        }

        @Test
        public void testName() {
            assertEquals("Lil Uzi Vert", extractor.getName());
        }

        @Test
        public void testId() throws ParsingException {
            assertEquals("10494998", extractor.getId());
            assertEquals("10494998", tabExtractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://soundcloud.com/liluzivert", extractor.getUrl());
            assertEquals("https://soundcloud.com/liluzivert/tracks", tabExtractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("http://soundcloud.com/liluzivert/sets", extractor.getOriginalUrl());
            assertEquals("http://soundcloud.com/liluzivert/tracks", tabExtractor.getOriginalUrl());
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
        public void testDescription() {
            assertNotNull(extractor.getDescription());
        }

        @Test
        public void testAvatarUrl() {
            assertIsSecureUrl(extractor.getAvatarUrl());
        }

        @Test
        public void testBannerUrl() {
            assertIsSecureUrl(extractor.getBannerUrl());
        }

        @Test
        public void testFeedUrl() {
            assertEmpty(extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() {
            assertTrue(extractor.getSubscriberCount() >= 1e6, "Wrong subscriber count");
        }

        @Test
        public void testVerified() throws Exception {
            assertTrue(extractor.isVerified());
        }

        @Test
        public void testTabs() throws Exception {
            Set<String> tabs = extractor.getTabs().stream()
                    .map(linkHandler -> linkHandler.getContentFilters().get(0)).collect(Collectors.toSet());
            assertTrue(tabs.contains(ChannelTabs.TRACKS));
            assertTrue(tabs.contains(ChannelTabs.PLAYLISTS));
            assertTrue(tabs.contains(ChannelTabs.ALBUMS));
        }
    }

    public static class DubMatix implements BaseChannelExtractorTest {
        private static SoundcloudChannelExtractor extractor;
        private static ChannelTabExtractor tabExtractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (SoundcloudChannelExtractor) SoundCloud
                    .getChannelExtractor("https://soundcloud.com/dubmatix");
            extractor.fetchPage();

            tabExtractor = SoundCloud.getChannelTabExtractor(extractor.getTabs().get(0));
            tabExtractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Additional Testing
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testGetPageInNewExtractor() throws Exception {
            final ChannelExtractor newExtractor = SoundCloud.getChannelExtractor(extractor.getUrl());
            newExtractor.fetchPage();
            final ChannelTabExtractor newTabExtractor = SoundCloud.getChannelTabExtractor(newExtractor.getTabs().get(0));
            defaultTestGetPageInNewExtractor(tabExtractor, newTabExtractor);
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testServiceId() {
            assertEquals(SoundCloud.getServiceId(), extractor.getServiceId());
            assertEquals(SoundCloud.getServiceId(), tabExtractor.getServiceId());
        }

        @Test
        public void testName() throws ParsingException {
            assertEquals("dubmatix", extractor.getName());
        }

        @Test
        public void testId() throws ParsingException {
            assertEquals("542134", extractor.getId());
            assertEquals("542134", tabExtractor.getId());
        }

        @Test
        public void testUrl() throws ParsingException {
            assertEquals("https://soundcloud.com/dubmatix", extractor.getUrl());
        }

        @Test
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://soundcloud.com/dubmatix", extractor.getOriginalUrl());
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
        public void testDescription() {
            assertNotNull(extractor.getDescription());
        }

        @Test
        public void testAvatarUrl() {
            assertIsSecureUrl(extractor.getAvatarUrl());
        }

        @Test
        public void testBannerUrl() {
            assertIsSecureUrl(extractor.getBannerUrl());
        }

        @Test
        public void testFeedUrl() {
            assertEmpty(extractor.getFeedUrl());
        }

        @Test
        public void testSubscriberCount() {
            assertTrue(extractor.getSubscriberCount() >= 2e6, "Wrong subscriber count");
        }

        @Test
        public void testVerified() throws Exception {
            assertTrue(extractor.isVerified());
        }

        @Test
        public void testTabs() throws Exception {
            Set<String> tabs = extractor.getTabs().stream()
                    .map(linkHandler -> linkHandler.getContentFilters().get(0)).collect(Collectors.toSet());
            assertTrue(tabs.contains(ChannelTabs.TRACKS));
            assertTrue(tabs.contains(ChannelTabs.PLAYLISTS));
            assertTrue(tabs.contains(ChannelTabs.ALBUMS));
        }
    }
}
