package org.schabi.newpipe.extractor.services;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.*;
import static org.schabi.newpipe.extractor.StreamingService.LinkType;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public final class DefaultTests {
    public static void defaultTestListOfItems(StreamingService expectedService, List<? extends InfoItem> itemsList, List<Throwable> errors) throws ParsingException {
        assertFalse("List of items is empty", itemsList.isEmpty());
        assertFalse("List of items contains a null element", itemsList.contains(null));
        assertEmptyErrors("Errors during extraction", errors);

        for (InfoItem item : itemsList) {
            assertIsSecureUrl(item.getUrl());

            final String thumbnailUrl = item.getThumbnailUrl();
            if (!isNullOrEmpty(thumbnailUrl)) {
                assertIsSecureUrl(thumbnailUrl);
            }
            assertNotNull("InfoItem type not set: " + item, item.getInfoType());
            assertEquals("Unexpected item service id", expectedService.getServiceId(), item.getServiceId());
            assertNotEmpty("Item name not set: " + item, item.getName());

            if (item instanceof StreamInfoItem) {
                StreamInfoItem streamInfoItem = (StreamInfoItem) item;
                assertNotEmpty("Uploader name not set: " + item, streamInfoItem.getUploaderName());

//                assertNotEmpty("Uploader url not set: " + item, streamInfoItem.getUploaderUrl());
                final String uploaderUrl = streamInfoItem.getUploaderUrl();
                if (!isNullOrEmpty(uploaderUrl)) {
                    assertIsSecureUrl(uploaderUrl);
                    assertExpectedLinkType(expectedService, uploaderUrl, LinkType.CHANNEL);
                }

                assertExpectedLinkType(expectedService, streamInfoItem.getUrl(), LinkType.STREAM);

                if (!isNullOrEmpty(streamInfoItem.getTextualUploadDate())) {
                    final DateWrapper uploadDate = streamInfoItem.getUploadDate();
                    assertNotNull("No parsed upload date", uploadDate);
                    assertTrue("Upload date not in the past", uploadDate.date().before(Calendar.getInstance()));
                }

            } else if (item instanceof ChannelInfoItem) {
                final ChannelInfoItem channelInfoItem = (ChannelInfoItem) item;
                assertExpectedLinkType(expectedService, channelInfoItem.getUrl(), LinkType.CHANNEL);

            } else if (item instanceof PlaylistInfoItem) {
                final PlaylistInfoItem playlistInfoItem = (PlaylistInfoItem) item;
                assertExpectedLinkType(expectedService, playlistInfoItem.getUrl(), LinkType.PLAYLIST);
            }
        }
    }

    private static void assertExpectedLinkType(StreamingService expectedService, String url, LinkType expectedLinkType) throws ParsingException {
        final LinkType linkTypeByUrl = expectedService.getLinkTypeByUrl(url);

        assertNotEquals("Url is not recognized by its own service: \"" + url + "\"",
                LinkType.NONE, linkTypeByUrl);
        assertEquals("Service returned wrong link type for: \"" + url + "\"",
                expectedLinkType, linkTypeByUrl);
    }

    public static void assertOnlyContainsType(ListExtractor.InfoItemsPage<? extends InfoItem> items, InfoItem.InfoType expectedType) {
        for (InfoItem item : items.getItems()) {
            assertEquals("Item list contains unexpected info types",
                    expectedType, item.getInfoType());
        }
    }

    public static <T extends InfoItem> void assertNoMoreItems(ListExtractor<T> extractor) throws Exception {
        final ListExtractor.InfoItemsPage<T> initialPage = extractor.getInitialPage();
        assertFalse("More items available when it shouldn't", initialPage.hasNextPage());
    }

    public static void assertNoDuplicatedItems(StreamingService expectedService,
                                               ListExtractor.InfoItemsPage<InfoItem> page1,
                                               ListExtractor.InfoItemsPage<InfoItem> page2) throws Exception {
        defaultTestListOfItems(expectedService, page1.getItems(), page1.getErrors());
        defaultTestListOfItems(expectedService, page2.getItems(), page2.getErrors());

        final Set<String> urlsSet = new HashSet<>();
        for (InfoItem item : page1.getItems()) {
            urlsSet.add(item.getUrl());
        }

        for (InfoItem item : page2.getItems()) {
            final boolean wasAdded = urlsSet.add(item.getUrl());
            if (!wasAdded) {
                fail("Same item was on the first and second page item list");
            }
        }
    }

    public static <T extends InfoItem> ListExtractor.InfoItemsPage<T> defaultTestRelatedItems(ListExtractor<T> extractor) throws Exception {
        final ListExtractor.InfoItemsPage<T> page = extractor.getInitialPage();
        final List<T> itemsList = page.getItems();
        List<Throwable> errors = page.getErrors();

        defaultTestListOfItems(extractor.getService(), itemsList, errors);
        return page;
    }

    public static <T extends InfoItem> ListExtractor.InfoItemsPage<T> defaultTestMoreItems(ListExtractor<T> extractor) throws Exception {
        final ListExtractor.InfoItemsPage<T> initialPage = extractor.getInitialPage();
        assertTrue("Doesn't have more items", initialPage.hasNextPage());
        ListExtractor.InfoItemsPage<T> nextPage = extractor.getPage(initialPage.getNextPage());
        final List<T> items = nextPage.getItems();
        assertFalse("Next page is empty", items.isEmpty());
        assertEmptyErrors("Next page have errors", nextPage.getErrors());

        defaultTestListOfItems(extractor.getService(), nextPage.getItems(), nextPage.getErrors());
        return nextPage;
    }

    public static void defaultTestGetPageInNewExtractor(ListExtractor<? extends InfoItem> extractor, ListExtractor<? extends InfoItem> newExtractor) throws Exception {
        final Page nextPage = extractor.getInitialPage().getNextPage();

        final ListExtractor.InfoItemsPage<? extends InfoItem> page = newExtractor.getPage(nextPage);
        defaultTestListOfItems(extractor.getService(), page.getItems(), page.getErrors());
    }
}
