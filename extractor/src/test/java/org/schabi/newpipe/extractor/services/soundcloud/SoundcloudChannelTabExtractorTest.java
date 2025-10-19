package org.schabi.newpipe.extractor.services.soundcloud;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmptyErrors;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestGetPageInNewExtractor;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.services.DefaultListExtractorTest;

import java.util.List;

class SoundcloudChannelTabExtractorTest {

    static class Tracks extends DefaultListExtractorTest<ChannelTabExtractor> {
        @Override
        protected ChannelTabExtractor createExtractor() throws Exception {
            return SoundCloud.getChannelTabExtractorFromId("10494998", ChannelTabs.TRACKS);
        }

        @Override public StreamingService expectedService() throws Exception { return SoundCloud; }
        @Override public String expectedName() throws Exception { return ChannelTabs.TRACKS; }
        @Override public String expectedId() throws Exception { return "10494998"; }
        @Override public String expectedUrlContains() throws Exception { return "https://soundcloud.com/liluzivert/tracks"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://soundcloud.com/liluzivert/tracks"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }

        @Test
        void testGetPageInNewExtractor() throws Exception {
            final ChannelTabExtractor newTabExtractor =
                    SoundCloud.getChannelTabExtractorFromId("10494998", ChannelTabs.TRACKS);
            defaultTestGetPageInNewExtractor(extractor(), newTabExtractor);
        }
    }

    static class Playlists extends DefaultListExtractorTest<ChannelTabExtractor> {
        @Override
        protected ChannelTabExtractor createExtractor() throws Exception {
            return SoundCloud.getChannelTabExtractorFromId("157193072", ChannelTabs.PLAYLISTS);
        }

        @Override public StreamingService expectedService() throws Exception { return SoundCloud; }
        @Override public String expectedName() throws Exception { return ChannelTabs.PLAYLISTS; }
        @Override public String expectedId() throws Exception { return "157193072"; }
        @Override public String expectedUrlContains() throws Exception { return "https://soundcloud.com/neffexmusic/sets"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://soundcloud.com/neffexmusic/sets"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.PLAYLIST; }
    }

    static class Albums extends DefaultListExtractorTest<ChannelTabExtractor> {
        @Override
        protected ChannelTabExtractor createExtractor() throws Exception {
            return SoundCloud.getChannelTabExtractorFromId("4803918", ChannelTabs.ALBUMS);
        }

        @Override public StreamingService expectedService() throws Exception { return SoundCloud; }
        @Override public String expectedName() throws Exception { return ChannelTabs.ALBUMS; }
        @Override public String expectedId() throws Exception { return "4803918"; }
        @Override public String expectedUrlContains() throws Exception { return "https://soundcloud.com/bigsean-1/albums"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://soundcloud.com/bigsean-1/albums"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.PLAYLIST; }
    }

    static class LikesOnlyTracks extends DefaultListExtractorTest<ChannelTabExtractor> {
        @Override
        protected ChannelTabExtractor createExtractor() throws Exception {
            return SoundCloud.getChannelTabExtractorFromId("30854092", ChannelTabs.LIKES);
        }

        @Override public StreamingService expectedService() throws Exception { return SoundCloud; }
        @Override public String expectedName() throws Exception { return ChannelTabs.LIKES; }
        @Override public String expectedId() throws Exception { return "30854092"; }
        @Override public String expectedUrlContains() throws Exception { return "https://soundcloud.com/lubenitza/likes"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://soundcloud.com/lubenitza/likes"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
    }

    static class LikesOnlyPlaylists extends DefaultListExtractorTest<ChannelTabExtractor> {
        @Override
        protected ChannelTabExtractor createExtractor() throws Exception {
            return SoundCloud.getChannelTabExtractorFromId("1280839267", ChannelTabs.LIKES);
        }

        @Override public StreamingService expectedService() throws Exception { return SoundCloud; }
        @Override public String expectedName() throws Exception { return ChannelTabs.LIKES; }
        @Override public String expectedId() throws Exception { return "1280839267"; }
        @Override public String expectedUrlContains() throws Exception { return "https://soundcloud.com/soreen-735855039/likes"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://soundcloud.com/soreen-735855039/likes"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.PLAYLIST; }
    }

    static class NoEmptyNextPages extends DefaultListExtractorTest<ChannelTabExtractor> {
        @Override
        protected ChannelTabExtractor createExtractor() throws Exception {
            return SoundCloud.getChannelTabExtractorFromId("73637815", ChannelTabs.TRACKS);
        }

        @Override public StreamingService expectedService() throws Exception { return SoundCloud; }
        @Override public String expectedName() throws Exception { return ChannelTabs.TRACKS; }
        @Override public String expectedId() throws Exception { return "73637815"; }
        @Override public String expectedUrlContains() throws Exception { return "https://soundcloud.com/hurtbox/tracks"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://soundcloud.com/hurtbox/tracks"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }

        @Test
        public void testNextPages() throws Exception {
            ListExtractor.InfoItemsPage<InfoItem> page = extractor().getInitialPage();
            for  (int i = 1; i <= 5; i++) {
                assertEmptyErrors("Next page has errors", page.getErrors());
                assertFalse(page.getItems().isEmpty(), "Next page is empty");
                assertTrue(page.hasNextPage(), "Next page does not have more items");
                page = extractor().getPage(page.getNextPage());
            }
        }

    }
}
