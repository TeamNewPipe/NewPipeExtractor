package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.schabi.newpipe.FileUtils.resolveTestResource;
import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.shared.extractors.YoutubeTakeoutSubscriptionExtractor;
import org.schabi.newpipe.extractor.services.youtube.youtube.YoutubeInstance;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionItem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Test for {@link YoutubeTakeoutSubscriptionExtractor}
 */
public class YoutubeTakeoutSubscriptionExtractorTest {


    private static YoutubeTakeoutSubscriptionExtractor subscriptionExtractor;
    private static LinkHandlerFactory urlHandler;

    @BeforeAll
    public static void setupClass() {
        //Doesn't make network requests
        NewPipe.init(DownloaderTestImpl.getInstance());
        subscriptionExtractor = new YoutubeTakeoutSubscriptionExtractor(
                YoutubeInstance.YOUTUBE.getNewStreamingService(0));
        urlHandler = ServiceList.YouTube.getChannelLHFactory();
    }

    @Test
    void testFromInputStream() throws Exception {
        final List<SubscriptionItem> subscriptionItems = subscriptionExtractor.fromInputStream(
                new FileInputStream(resolveTestResource("youtube_takeout_import_test.json")));
        assertEquals(7, subscriptionItems.size());

        for (final SubscriptionItem item : subscriptionItems) {
            assertNotNull(item.getName());
            assertNotNull(item.getUrl());
            assertTrue(urlHandler.acceptUrl(item.getUrl()));
            assertEquals(ServiceList.YouTube.getServiceId(), item.getServiceId());
        }
    }

    @Test
    void testEmptySourceException() throws Exception {
        final List<SubscriptionItem> items = subscriptionExtractor.fromInputStream(
                new ByteArrayInputStream("[]".getBytes(UTF_8)));
        assertTrue(items.isEmpty());
    }

    @Test
    void testSubscriptionWithEmptyTitleInSource() throws Exception {
        final String source = "[{\"snippet\":{\"resourceId\":{\"channelId\":\"UCEOXxzW2vU0P-0THehuIIeg\"}}}]";
        final List<SubscriptionItem> items = subscriptionExtractor.fromInputStream(
                new ByteArrayInputStream(source.getBytes(UTF_8)));

        assertEquals(1, items.size());
        assertEquals(ServiceList.YouTube.getServiceId(), items.get(0).getServiceId());
        assertEquals("https://www.youtube.com/channel/UCEOXxzW2vU0P-0THehuIIeg", items.get(0).getUrl());
        assertEquals("", items.get(0).getName());
    }

    @Test
    void testSubscriptionWithInvalidUrlInSource() throws Exception {
        final String source = "[{\"snippet\":{\"resourceId\":{\"channelId\":\"gibberish\"},\"title\":\"name1\"}}," +
                "{\"snippet\":{\"resourceId\":{\"channelId\":\"UCEOXxzW2vU0P-0THehuIIeg\"},\"title\":\"name2\"}}]";
        final List<SubscriptionItem> items = subscriptionExtractor.fromInputStream(
                new ByteArrayInputStream(source.getBytes(UTF_8)));

        assertEquals(1, items.size());
        assertEquals(ServiceList.YouTube.getServiceId(), items.get(0).getServiceId());
        assertEquals("https://www.youtube.com/channel/UCEOXxzW2vU0P-0THehuIIeg", items.get(0).getUrl());
        assertEquals("name2", items.get(0).getName());
    }

    @Test
    void testInvalidSourceException() {
        final List<String> invalidList = Arrays.asList(
                "<xml><notvalid></notvalid></xml>",
                "<opml><notvalid></notvalid></opml>",
                "{\"a\":\"b\"}",
                "[{}]",
                "[\"\", 5]",
                "[{\"snippet\":{\"title\":\"name\"}}]",
                "[{\"snippet\":{\"resourceId\":{\"channelId\":\"gibberish\"}}}]",
                "",
                "\uD83D\uDC28\uD83D\uDC28\uD83D\uDC28",
                "gibberish");

        for (final String invalidContent : invalidList) {
            try {
                final byte[] bytes = invalidContent.getBytes(UTF_8);
                subscriptionExtractor.fromInputStream(new ByteArrayInputStream(bytes));
                fail("Extracting from \"" + invalidContent + "\" didn't throw an exception");
            } catch (final Exception e) {
                final boolean correctType = e instanceof SubscriptionExtractor.InvalidSourceException;
                if (!correctType) {
                    e.printStackTrace();
                }
                assertTrue(correctType, e.getClass().getSimpleName() + " is not InvalidSourceException");
            }
        }
    }

    private static void assertSubscriptionItems(final List<SubscriptionItem> subscriptionItems)
            throws Exception {
        assertTrue(subscriptionItems.size() > 0);

        for (final SubscriptionItem item : subscriptionItems) {
            assertNotNull(item.getName());
            assertNotNull(item.getUrl());
            assertTrue(urlHandler.acceptUrl(item.getUrl()));
            assertEquals(ServiceList.YouTube.getServiceId(), item.getServiceId());
        }
    }

    @Test
    void fromZipInputStream() throws Exception {
        final List<String> zipPaths = Arrays.asList(
                "youtube_takeout_import_test_1.zip",
                "youtube_takeout_import_test_2.zip"
        );

        for (final String path : zipPaths) {
            final File file = resolveTestResource(path);
            final FileInputStream fileInputStream = new FileInputStream(file);
            final List<SubscriptionItem> subscriptionItems = subscriptionExtractor.fromZipInputStream(fileInputStream);
            assertSubscriptionItems(subscriptionItems);
        }
    }

    @Test
    void fromCsvInputStream() throws Exception {
        final List<String> csvPaths = Arrays.asList(
                "youtube_takeout_import_test_1.csv",
                "youtube_takeout_import_test_2.csv"
        );

        for (final String path : csvPaths) {
            final File file = resolveTestResource(path);
            final FileInputStream fileInputStream = new FileInputStream(file);
            final List<SubscriptionItem> subscriptionItems = subscriptionExtractor.fromCsvInputStream(fileInputStream);
            assertSubscriptionItems(subscriptionItems);
        }
    }
}
