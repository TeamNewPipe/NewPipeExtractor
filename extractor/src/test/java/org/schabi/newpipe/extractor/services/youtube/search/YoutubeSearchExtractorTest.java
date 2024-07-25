package org.schabi.newpipe.extractor.services.youtube.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmptyErrors;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.assertNoDuplicatedItems;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.CHANNELS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.PLAYLISTS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.VIDEOS;
import static java.util.Collections.singletonList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.YoutubeTestsUtils;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public class YoutubeSearchExtractorTest {

    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/search/";

    public static class All extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "test";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "all"));
            extractor = YouTube.getSearchExtractor(QUERY);
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
    }

    public static class Channel extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "test";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "channel"));
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(CHANNELS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }

        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.CHANNEL; }
    }

    public static class Playlists extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "test";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "playlist"));
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(PLAYLISTS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }

        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.PLAYLIST; }
    }

    public static class Videos extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "test";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "videos"));
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(VIDEOS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }

        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
    }

    /**
     * Test for YT's "Did you mean...".
     *
     * <p>
     * Hint: YT mostly shows "did you mean..." when you are searching in another language.
     * </p>
     */
    public static class Suggestion extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "on board ing";
        private static final String EXPECTED_SUGGESTION = "on boarding";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "suggestions"));
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(VIDEOS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() throws Exception { return "youtube.com/results?search_query=" + Utils.encodeUrlUtf8(QUERY); }
        @Override public String expectedOriginalUrlContains() throws Exception { return "youtube.com/results?search_query=" + Utils.encodeUrlUtf8(QUERY); }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return EXPECTED_SUGGESTION; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
    }

    /**
     * Test for YT's "Showing results for...".
     */
    public static class CorrectedSearch extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "pewdeipie";
        private static final String EXPECTED_SUGGESTION = "pewdiepie";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "corrected"));
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(VIDEOS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return EXPECTED_SUGGESTION; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
        @Override public boolean isCorrectedSearch() { return true; }
    }

    public static class RandomQueryNoMorePages extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "UCO6AK";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "random"));
            extractor = YouTube.getSearchExtractor(QUERY);
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }

        /*//////////////////////////////////////////////////////////////////////////
        // Test Overrides
        //////////////////////////////////////////////////////////////////////////*/

        @Test
        public void testMoreRelatedItems() throws Exception {
            final ListExtractor.InfoItemsPage<InfoItem> initialPage = extractor().getInitialPage();
            // YouTube actually gives us an empty next page, but after that, no more pages.
            assertTrue(initialPage.hasNextPage());
            final ListExtractor.InfoItemsPage<InfoItem> nextEmptyPage = extractor.getPage(initialPage.getNextPage());
            assertEquals(0, nextEmptyPage.getItems().size());
            assertEmptyErrors("Empty page has errors", nextEmptyPage.getErrors());

            assertFalse(nextEmptyPage.hasNextPage(), "More items available when it shouldn't");
        }
    }

    static class PagingTest {
        @Test
        void duplicatedItemsCheck() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "paging"));
            final SearchExtractor extractor = YouTube.getSearchExtractor("cirque du soleil", singletonList(VIDEOS), "");
            extractor.fetchPage();

            final ListExtractor.InfoItemsPage<InfoItem> page1 = extractor.getInitialPage();
            final ListExtractor.InfoItemsPage<InfoItem> page2 = extractor.getPage(page1.getNextPage());

            assertNoDuplicatedItems(YouTube, page1, page2);
        }
    }

    public static class MetaInfoTest extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "Covid";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "metaInfo"));
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(VIDEOS), "");
            extractor.fetchPage();
        }

        @Override public String expectedSearchString() { return QUERY; }
        @Override public String expectedSearchSuggestion() { return null; }
        @Override public List<MetaInfo> expectedMetaInfo() throws MalformedURLException {
            return Collections.singletonList(new MetaInfo(
                    "COVID-19",
                    new Description(
                            "Get the latest information from the WHO about coronavirus.",
                            Description.PLAIN_TEXT),
                    Collections.singletonList(
                            new URL("https://www.who.int/emergencies/diseases/novel-coronavirus-2019")),
                    Collections.singletonList("Learn more")
            ));
        }
        // testMoreRelatedItems is broken because a video has no duration shown
        @Test @Override public void testMoreRelatedItems() { }
        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedOriginalUrlContains() throws Exception { return "youtube.com/results?search_query=" + QUERY; }
    }

    public static class ChannelVerified extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "bbc";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "verified"));
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(CHANNELS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.CHANNEL; }

        @Test
        void testAtLeastOneVerified() throws IOException, ExtractionException {
            final List<InfoItem> items = extractor.getInitialPage().getItems();
            boolean verified = false;
            for (InfoItem item : items) {
                if (((ChannelInfoItem) item).isVerified()) {
                    verified = true;
                    break;
                }
            }

            assertTrue(verified);
        }
    }

    public static class VideoUploaderAvatar extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "sidemen";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "video_uploader_avatar"));
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(VIDEOS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }

        @Test
        void testUploaderAvatars() throws IOException, ExtractionException {
            extractor.getInitialPage()
                    .getItems()
                    .stream()
                    .filter(StreamInfoItem.class::isInstance)
                    .map(StreamInfoItem.class::cast)
                    .forEach(streamInfoItem ->
                            YoutubeTestsUtils.testImages(streamInfoItem.getUploaderAvatars()));
        }
    }

    public static class VideoDescription extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "44wLAzydRFU";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "video_description"));
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(VIDEOS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "youtube.com/results?search_query=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }

        @Test
        void testVideoDescription() throws IOException, ExtractionException {
            final List<InfoItem> items = extractor.getInitialPage().getItems();
            assertNotNull(((StreamInfoItem) items.get(0)).getShortDescription());
        }
    }

    public static class ShortFormContent extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "#shorts";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "shorts"));
            extractor = YouTube.getSearchExtractor(QUERY, singletonList(VIDEOS), "");
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() throws Exception { return "youtube.com/results?search_query=" + Utils.encodeUrlUtf8(QUERY); }
        @Override public String expectedOriginalUrlContains() throws Exception { return "youtube.com/results?search_query=" + Utils.encodeUrlUtf8(QUERY); }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
        @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }

        @Test
        void testShortFormContent() throws IOException, ExtractionException {
            assertTrue(extractor.getInitialPage()
                    .getItems()
                    .stream()
                    .filter(StreamInfoItem.class::isInstance)
                    .map(StreamInfoItem.class::cast)
                    .anyMatch(StreamInfoItem::isShortFormContent));
        }
    }

    /**
     * A {@link SearchExtractor} test to check if crisis resources preventing search results to be
     * returned are bypassed (searches with content filters are not tested in this test, even if
     * they should work as bypasses are used with them too).
     *
     * <p>
     * See <a href="https://support.google.com/youtube/answer/10726080?hl=en">
     * https://support.google.com/youtube/answer/10726080?hl=en</a> for more info on crisis
     * resources.
     * </p>
     */
    public static class CrisisResources extends DefaultSearchExtractorTest {
        private static SearchExtractor extractor;
        private static final String QUERY = "suicide";

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "crisis_resources"));
            extractor = YouTube.getSearchExtractor(QUERY);
            extractor.fetchPage();
        }

        @Override public SearchExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() throws Exception { return "youtube.com/results?search_query=" + Utils.encodeUrlUtf8(QUERY); }
        @Override public String expectedOriginalUrlContains() throws Exception { return "youtube.com/results?search_query=" + Utils.encodeUrlUtf8(QUERY); }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }

        @Test
        @Override
        public void testMetaInfo() throws Exception {
            final List<MetaInfo> metaInfoList = extractor().getMetaInfo();

            // the meta info will have different text and language depending on where in the world
            // the connection is established from, so we can't check the actual content
            assertEquals(1, metaInfoList.size());
        }
    }
}
