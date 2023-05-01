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

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (SoundcloudChannelExtractor) SoundCloud
                    .getChannelExtractor("http://soundcloud.com/liluzivert/sets");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        @Override
        public void testServiceId() {
            assertEquals(SoundCloud.getServiceId(), extractor.getServiceId());
        }

        @Test
        @Override
        public void testName() {
            assertEquals("Lil Uzi Vert", extractor.getName());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("10494998", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://soundcloud.com/liluzivert", extractor.getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws ParsingException {
            assertEquals("http://soundcloud.com/liluzivert/sets", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ChannelExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        @Override
        public void testDescription() {
            assertNotNull(extractor.getDescription());
        }

        @Test
        @Override
        public void testAvatarUrl() {
            assertIsSecureUrl(extractor.getAvatarUrl());
        }

        @Test
        @Override
        public void testBannerUrl() {
            assertIsSecureUrl(extractor.getBannerUrl());
        }

        @Test
        @Override
        public void testFeedUrl() {
            assertEmpty(extractor.getFeedUrl());
        }

        @Test
        @Override
        public void testSubscriberCount() {
            assertTrue(extractor.getSubscriberCount() >= 1e6, "Wrong subscriber count");
        }

        @Test
        @Override
        public void testVerified() throws Exception {
            assertTrue(extractor.isVerified());
        }

        @Test
        @Override
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

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (SoundcloudChannelExtractor) SoundCloud
                    .getChannelExtractor("https://soundcloud.com/dubmatix");
            extractor.fetchPage();
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        @Override
        public void testServiceId() {
            assertEquals(SoundCloud.getServiceId(), extractor.getServiceId());
        }

        @Test
        @Override
        public void testName() throws ParsingException {
            assertEquals("dubmatix", extractor.getName());
        }

        @Test
        @Override
        public void testId() throws ParsingException {
            assertEquals("542134", extractor.getId());
        }

        @Test
        @Override
        public void testUrl() throws ParsingException {
            assertEquals("https://soundcloud.com/dubmatix", extractor.getUrl());
        }

        @Test
        @Override
        public void testOriginalUrl() throws ParsingException {
            assertEquals("https://soundcloud.com/dubmatix", extractor.getOriginalUrl());
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ChannelExtractor
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        @Override
        public void testDescription() {
            assertNotNull(extractor.getDescription());
        }

        @Test
        @Override
        public void testAvatarUrl() {
            assertIsSecureUrl(extractor.getAvatarUrl());
        }

        @Test
        @Override
        public void testBannerUrl() {
            assertIsSecureUrl(extractor.getBannerUrl());
        }

        @Test
        @Override
        public void testFeedUrl() {
            assertEmpty(extractor.getFeedUrl());
        }

        @Test
        @Override
        public void testSubscriberCount() {
            assertTrue(extractor.getSubscriberCount() >= 2e6, "Wrong subscriber count");
        }

        @Test
        @Override
        public void testVerified() throws Exception {
            assertTrue(extractor.isVerified());
        }

        @Test
        @Override
        public void testTabs() throws Exception {
            Set<String> tabs = extractor.getTabs().stream()
                    .map(linkHandler -> linkHandler.getContentFilters().get(0)).collect(Collectors.toSet());
            assertTrue(tabs.contains(ChannelTabs.TRACKS));
            assertTrue(tabs.contains(ChannelTabs.PLAYLISTS));
            assertTrue(tabs.contains(ChannelTabs.ALBUMS));
        }
    }
}
