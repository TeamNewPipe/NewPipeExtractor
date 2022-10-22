package org.schabi.newpipe.extractor.services.youtube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabHandler;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelTabExtractor;

import java.io.IOException;

import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestMoreItems;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

public class YouTubeChannelTabExtractorTest {
    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/channelTab/";

    public static class Playlists {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        public static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "playlists"));
            extractor = (YoutubeChannelTabExtractor) YouTube.getChannelTabExtractorFromId("UC2DjFE7Xf11URZqWBigcVOQ", ChannelTabHandler.Tab.Playlists);
            extractor.fetchPage();
        }

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }
    }

    public static class Channels {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        public static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "channels"));
            extractor = (YoutubeChannelTabExtractor) YouTube.getChannelTabExtractorFromId("UC2DjFE7Xf11URZqWBigcVOQ", ChannelTabHandler.Tab.Channels);
            extractor.fetchPage();
        }

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }
    }

    public static class Livestreams {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        public static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            YoutubeParsingHelper.setVisitorData(YoutubeTestsUtils.VISITOR_DATA_NEW_CHANNEL_LAYOUT);
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "livestreams"));
            extractor = (YoutubeChannelTabExtractor) YouTube.getChannelTabExtractorFromId("UCR-DXc1voovS8nhAvccRZhg", ChannelTabHandler.Tab.Livestreams);
            extractor.fetchPage();
        }

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }
    }

    public static class Shorts {
        private static YoutubeChannelTabExtractor extractor;

        @BeforeAll
        public static void setUp() throws IOException, ExtractionException {
            YoutubeTestsUtils.ensureStateless();
            YoutubeParsingHelper.setVisitorData(YoutubeTestsUtils.VISITOR_DATA_NEW_CHANNEL_LAYOUT);
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "shorts"));
            extractor = (YoutubeChannelTabExtractor) YouTube.getChannelTabExtractorFromId("UCh8gHdtzO2tXd593_bjErWg", ChannelTabHandler.Tab.Shorts);
            extractor.fetchPage();
        }

        @Test
        public void testRelatedItems() throws Exception {
            defaultTestRelatedItems(extractor);
        }

        @Test
        public void testMoreRelatedItems() throws Exception {
            defaultTestMoreItems(extractor);
        }
    }
}
