package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSubscriptionExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionItem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test for {@link YoutubeSubscriptionExtractor}
 */
public class YoutubeSubscriptionExtractorTest {
    private static YoutubeSubscriptionExtractor subscriptionExtractor;
    private static LinkHandlerFactory urlHandler;

    @BeforeClass
    public static void setupClass() {
        NewPipe.init(Downloader.getInstance());
        subscriptionExtractor = new YoutubeSubscriptionExtractor(ServiceList.YouTube);
        urlHandler = ServiceList.YouTube.getChannelLHFactory();
    }

    @Test
    public void testFromInputStream() throws Exception {
        File testFile = new File("extractor/src/test/resources/youtube_export_test.xml");
        if (!testFile.exists()) testFile = new File("src/test/resources/youtube_export_test.xml");

        List<SubscriptionItem> subscriptionItems = subscriptionExtractor.fromInputStream(new FileInputStream(testFile));
        assertTrue("List doesn't have exactly 8 items (had " + subscriptionItems.size() + ")", subscriptionItems.size() == 8);

        for (SubscriptionItem item : subscriptionItems) {
            assertNotNull(item.getName());
            assertNotNull(item.getUrl());
            assertTrue(urlHandler.acceptUrl(item.getUrl()));
            assertFalse(item.getServiceId() == -1);
        }
    }

    @Test
    public void testEmptySourceException() throws Exception {
        String emptySource = "<opml version=\"1.1\"><body>" +
                "<outline text=\"Testing\" title=\"123\" />" +
                "</body></opml>";

        List<SubscriptionItem> items = subscriptionExtractor.fromInputStream(new ByteArrayInputStream(emptySource.getBytes("UTF-8")));
        assertTrue(items.isEmpty());
    }

    @Test
    public void testInvalidSourceException() {
        List<String> invalidList = Arrays.asList(
                "<xml><notvalid></notvalid></xml>",
                "<opml><notvalid></notvalid></opml>",
                "<opml><body></body></opml>",
                "<opml><body><outline text=\"fail\" title=\"fail\" type=\"rss\" xmlUgrl=\"invalidTag\"/></outline></body></opml>",
                "<opml><body><outline><outline text=\"invalid\" title=\"url\" type=\"rss\"" +
                        " xmlUrl=\"https://www.youtube.com/feeds/videos.xml?channel_not_id=|||||||\"/></outline></body></opml>",
                "",
                null,
                "\uD83D\uDC28\uD83D\uDC28\uD83D\uDC28",
                "gibberish");

        for (String invalidContent : invalidList) {
            try {
                if (invalidContent != null) {
                    byte[] bytes = invalidContent.getBytes("UTF-8");
                    subscriptionExtractor.fromInputStream(new ByteArrayInputStream(bytes));
                } else {
                    subscriptionExtractor.fromInputStream(null);
                }

                fail("didn't throw exception");
            } catch (Exception e) {
                // System.out.println(" -> " + e);
                boolean isExpectedException = e instanceof SubscriptionExtractor.InvalidSourceException;
                assertTrue("\"" + e.getClass().getSimpleName() + "\" is not the expected exception", isExpectedException);
            }
        }
    }
}
