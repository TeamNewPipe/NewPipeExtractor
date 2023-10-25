package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.DefaultListExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelTabExtractor;

import java.io.IOException;

class YoutubeChannelTabExtractorTest {

    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH
            + "services/youtube/extractor/channelTabs/";

    static class Videos extends DefaultListExtractorTest<ChannelTabExtractor> {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "videos"));
            extractor = (YoutubeChannelTabExtractor) YouTube.getChannelTabExtractorFromId(
                    "user/creativecommons", ChannelTabs.VIDEOS);
            extractor.fetchPage();
        }

        @Override public ChannelTabExtractor extractor() throws Exception { return extractor; }
        @Override public StreamingService expectedService() throws Exception { return YouTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.VIDEOS; }
        @Override public String expectedId() throws Exception { return "UCTwECeGqMZee77BjdoYtI2Q"; }
        @Override public String expectedUrlContains() throws Exception { return "https://www.youtube.com/channel/UCTwECeGqMZee77BjdoYtI2Q/videos"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://www.youtube.com/user/creativecommons/videos"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
    }

    static class Playlists extends DefaultListExtractorTest<ChannelTabExtractor> {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "playlists"));
            extractor = (YoutubeChannelTabExtractor) YouTube.getChannelTabExtractorFromId(
                    "@EEVblog", ChannelTabs.PLAYLISTS);
            extractor.fetchPage();
        }

        @Override public ChannelTabExtractor extractor() throws Exception { return extractor; }
        @Override public StreamingService expectedService() throws Exception { return YouTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.PLAYLISTS; }
        @Override public String expectedId() throws Exception { return "UC2DjFE7Xf11URZqWBigcVOQ"; }
        @Override public String expectedUrlContains() throws Exception { return "https://www.youtube.com/channel/UC2DjFE7Xf11URZqWBigcVOQ/playlists"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://www.youtube.com/@EEVblog/playlists"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.PLAYLIST; }
    }

    static class Channels extends DefaultListExtractorTest<ChannelTabExtractor> {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "channels"));
            extractor = (YoutubeChannelTabExtractor) YouTube.getChannelTabExtractorFromId(
                    "channel/UC2DjFE7Xf11URZqWBigcVOQ", ChannelTabs.CHANNELS);
            extractor.fetchPage();
        }

        @Override public ChannelTabExtractor extractor() throws Exception { return extractor; }
        @Override public StreamingService expectedService() throws Exception { return YouTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.CHANNELS; }
        @Override public String expectedId() throws Exception { return "UC2DjFE7Xf11URZqWBigcVOQ"; }
        @Override public String expectedUrlContains() throws Exception { return "https://www.youtube.com/channel/UC2DjFE7Xf11URZqWBigcVOQ/channels"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://www.youtube.com/channel/UC2DjFE7Xf11URZqWBigcVOQ/channels"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.CHANNEL; }
    }

    static class Livestreams extends DefaultListExtractorTest<ChannelTabExtractor> {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "livestreams"));
            extractor = (YoutubeChannelTabExtractor) YouTube.getChannelTabExtractorFromId(
                    "c/JeffGeerling", ChannelTabs.LIVESTREAMS);
            extractor.fetchPage();
        }

        @Override public ChannelTabExtractor extractor() throws Exception { return extractor; }
        @Override public StreamingService expectedService() throws Exception { return YouTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.LIVESTREAMS; }
        @Override public String expectedId() throws Exception { return "UCR-DXc1voovS8nhAvccRZhg"; }
        @Override public String expectedUrlContains() throws Exception { return "https://www.youtube.com/channel/UCR-DXc1voovS8nhAvccRZhg/streams"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://www.youtube.com/c/JeffGeerling/streams"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
    }

    static class Shorts extends DefaultListExtractorTest<ChannelTabExtractor> {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "shorts"));
            extractor = (YoutubeChannelTabExtractor) YouTube.getChannelTabExtractorFromId(
                    "channel/UCh8gHdtzO2tXd593_bjErWg", ChannelTabs.SHORTS);
            extractor.fetchPage();
        }

        @Override public ChannelTabExtractor extractor() throws Exception { return extractor; }
        @Override public StreamingService expectedService() throws Exception { return YouTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.SHORTS; }
        @Override public String expectedId() throws Exception { return "UCh8gHdtzO2tXd593_bjErWg"; }
        @Override public String expectedUrlContains() throws Exception { return "https://www.youtube.com/channel/UCh8gHdtzO2tXd593_bjErWg/shorts"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://www.youtube.com/channel/UCh8gHdtzO2tXd593_bjErWg/shorts"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
    }

    private static abstract class AgeRestrictedTabsVideosBaseTest
            extends DefaultListExtractorTest<ChannelTabExtractor> {

        protected static ChannelTabExtractor extractor;

        @Override public ChannelTabExtractor extractor() throws Exception { return extractor; }
        @Override public StreamingService expectedService() throws Exception { return YouTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.VIDEOS; }
        @Override public String expectedId() throws Exception { return "UCbfnHqxXs_K3kvaH-WlNlig"; }
        @Override public String expectedUrlContains() throws Exception { return "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/videos"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/videos"; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
    }

    static class AgeRestrictedTabsVideos extends AgeRestrictedTabsVideosBaseTest {
        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(
                    RESOURCE_PATH + "ageRestrictedTabsVideos"));
            extractor = YouTube.getChannelTabExtractorFromId(
                    "channel/UCbfnHqxXs_K3kvaH-WlNlig", ChannelTabs.VIDEOS);
            extractor.fetchPage();
        }
    }

    static class AgeRestrictedTabsShorts extends DefaultListExtractorTest<ChannelTabExtractor> {
        private static ChannelTabExtractor extractor;

        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(
                    RESOURCE_PATH + "ageRestrictedTabsShorts"));
            extractor = YouTube.getChannelTabExtractorFromId(
                    "channel/UCbfnHqxXs_K3kvaH-WlNlig", ChannelTabs.SHORTS);
            extractor.fetchPage();
        }

        @Override public ChannelTabExtractor extractor() throws Exception { return extractor; }
        @Override public StreamingService expectedService() throws Exception { return YouTube; }
        @Override public String expectedName() throws Exception { return ChannelTabs.SHORTS; }
        @Override public String expectedId() throws Exception { return "UCbfnHqxXs_K3kvaH-WlNlig"; }
        @Override public String expectedUrlContains() throws Exception { return "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/shorts"; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/shorts"; }
        @Override public boolean expectedHasMoreItems() { return false; }

        @Test
        @Override
        public void testRelatedItems() throws Exception {
            // This channel has no shorts, so an empty page should be returned by the playlist
            // extractor
            assertTrue(extractor.getInitialPage().getItems().isEmpty());
            assertTrue(extractor.getInitialPage().getErrors().isEmpty());
        }
    }

    static class AgeRestrictedTabsVideosFromChannel extends AgeRestrictedTabsVideosBaseTest {
        @BeforeAll
        static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(
                    RESOURCE_PATH + "ageRestrictedTabsVideosFromChannel"));
            final ChannelExtractor channelExtractor = YouTube.getChannelExtractor(
                    "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig");
            channelExtractor.fetchPage();

            // the videos tab is the first one
            extractor = YouTube.getChannelTabExtractor(channelExtractor.getTabs().get(0));
            extractor.fetchPage();
        }
    }
}
