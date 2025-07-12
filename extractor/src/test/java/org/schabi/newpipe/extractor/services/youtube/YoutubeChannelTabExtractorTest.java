package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.services.DefaultListExtractorTest;

class YoutubeChannelTabExtractorTest {

    static class Videos extends DefaultListExtractorTest<ChannelTabExtractor>
        implements InitYoutubeTest {

        @Override
        protected ChannelTabExtractor createExtractor() throws Exception {
            return YouTube.getChannelTabExtractorFromId(
                "user/creativecommons", ChannelTabs.VIDEOS);
        }

        @Override public StreamingService expectedService() throws Exception { return YouTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.VIDEOS; }
        @Override public String expectedId() throws Exception { return "UCTwECeGqMZee77BjdoYtI2Q"; }
        @Override public String expectedUrlContains() throws Exception { return "https://www.youtube.com/channel/UCTwECeGqMZee77BjdoYtI2Q/videos"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://www.youtube.com/user/creativecommons/videos"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
        @Override public boolean expectedHasMoreItems() { return true; }
    }

    static class Playlists extends DefaultListExtractorTest<ChannelTabExtractor>
        implements InitYoutubeTest {

        @Override
        protected ChannelTabExtractor createExtractor() throws Exception {
            return YouTube.getChannelTabExtractorFromId("@EEVblog", ChannelTabs.PLAYLISTS);
        }

        @Override public StreamingService expectedService() throws Exception { return YouTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.PLAYLISTS; }
        @Override public String expectedId() throws Exception { return "UC2DjFE7Xf11URZqWBigcVOQ"; }
        @Override public String expectedUrlContains() throws Exception { return "https://www.youtube.com/channel/UC2DjFE7Xf11URZqWBigcVOQ/playlists"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://www.youtube.com/@EEVblog/playlists"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.PLAYLIST; }
        @Override public boolean expectedHasMoreItems() { return true; }
    }

    static class Livestreams extends DefaultListExtractorTest<ChannelTabExtractor>
        implements InitYoutubeTest {

        @Override
        protected ChannelTabExtractor createExtractor() throws Exception {
            return YouTube.getChannelTabExtractorFromId(
                "c/JeffGeerling", ChannelTabs.LIVESTREAMS);
        }

        @Override public StreamingService expectedService() throws Exception { return YouTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.LIVESTREAMS; }
        @Override public String expectedId() throws Exception { return "UCR-DXc1voovS8nhAvccRZhg"; }
        @Override public String expectedUrlContains() throws Exception { return "https://www.youtube.com/channel/UCR-DXc1voovS8nhAvccRZhg/streams"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://www.youtube.com/c/JeffGeerling/streams"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
        @Override public boolean expectedHasMoreItems() { return true; }
    }

    static class Shorts extends DefaultListExtractorTest<ChannelTabExtractor>
        implements InitYoutubeTest {

        @Override
        protected ChannelTabExtractor createExtractor() throws Exception {
            return YouTube.getChannelTabExtractorFromId(
                "channel/UCh8gHdtzO2tXd593_bjErWg", ChannelTabs.SHORTS);
        }

        @Override public StreamingService expectedService() throws Exception { return YouTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.SHORTS; }
        @Override public String expectedId() throws Exception { return "UCh8gHdtzO2tXd593_bjErWg"; }
        @Override public String expectedUrlContains() throws Exception { return "https://www.youtube.com/channel/UCh8gHdtzO2tXd593_bjErWg/shorts"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://www.youtube.com/channel/UCh8gHdtzO2tXd593_bjErWg/shorts"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
        @Override public boolean expectedHasMoreItems() { return true; }
    }

    static class Albums extends DefaultListExtractorTest<ChannelTabExtractor>
        implements InitYoutubeTest {

        @Override
        protected ChannelTabExtractor createExtractor() throws Exception {
            return YouTube.getChannelTabExtractorFromId("@Radiohead", ChannelTabs.ALBUMS);
        }

        @Override public StreamingService expectedService() throws Exception { return YouTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.ALBUMS; }
        @Override public String expectedId() throws Exception { return "UCq19-LqvG35A-30oyAiPiqA"; }
        @Override public String expectedUrlContains() throws Exception { return "https://www.youtube.com/channel/UCq19-LqvG35A-30oyAiPiqA/releases"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://www.youtube.com/@Radiohead/releases"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.PLAYLIST; }
        @Override public boolean expectedHasMoreItems() { return true; }
    }


    // TESTS FOR TABS OF AGE RESTRICTED CHANNELS
    // Fetching the tabs individually would use the standard tabs without fallback to
    // system playlists for stream tabs, we need to fetch the channel extractor to get the
    // channel playlist tabs
    // TODO: implement system playlists fallback in YoutubeChannelTabExtractor for stream
    //  tabs

    static class AgeRestrictedTabsVideos extends DefaultListExtractorTest<ChannelTabExtractor>
        implements InitYoutubeTest {

        @Override
        protected ChannelTabExtractor createExtractor() throws Exception {
            final ChannelExtractor channelExtractor = YouTube.getChannelExtractor(
                "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig");
            channelExtractor.fetchPage();

            // the videos tab is the first one
            return YouTube.getChannelTabExtractor(channelExtractor.getTabs().get(0));
        }

        @Override public StreamingService expectedService() throws Exception { return YouTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.VIDEOS; }
        @Override public String expectedId() throws Exception { return "UCbfnHqxXs_K3kvaH-WlNlig"; }
        @Override public String expectedUrlContains() throws Exception { return "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/videos"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/videos"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
        @Override public boolean expectedHasMoreItems() { return true; }
    }

    static class AgeRestrictedTabsShorts extends DefaultListExtractorTest<ChannelTabExtractor>
        implements InitYoutubeTest {

        @Override
        protected ChannelTabExtractor createExtractor() throws Exception {
            final ChannelExtractor channelExtractor = YouTube.getChannelExtractor(
                "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig");
            channelExtractor.fetchPage();

            // the shorts tab is the second one
            return YouTube.getChannelTabExtractor(channelExtractor.getTabs().get(1));
        }

        @Override public StreamingService expectedService() throws Exception { return YouTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.SHORTS; }
        @Override public String expectedId() throws Exception { return "UCbfnHqxXs_K3kvaH-WlNlig"; }
        @Override public String expectedUrlContains() throws Exception { return "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/shorts"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/shorts"; }
        @Override public boolean expectedHasMoreItems() { return false; }

        @Test
        @Override
        public void testRelatedItems() throws Exception {
            // this channel has no shorts, so an empty page is returned by the playlist extractor
            assertTrue(extractor().getInitialPage().getItems().isEmpty());
            assertTrue(extractor().getInitialPage().getErrors().isEmpty());
        }
    }
}
