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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.*;
import static org.schabi.newpipe.extractor.StreamingService.LinkType;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public final class DefaultTests {
    public static void defaultTestListOfItems(StreamingService expectedService, List<? extends InfoItem> itemsList, List<Throwable> errors) throws ParsingException {
        assertFalse(itemsList.isEmpty(), "List of items is empty");
        assertFalse(itemsList.contains(null), "List of items contains a null element");
        assertEmptyErrors("Errors during extraction", errors);

        for (InfoItem item : itemsList) {
            assertIsSecureUrl(item.getUrl());

            final String thumbnailUrl = item.getThumbnailUrl();
            if (!isNullOrEmpty(thumbnailUrl)) {
                assertIsSecureUrl(thumbnailUrl);
            }
            assertNotNull(item.getInfoType(), "InfoItem type not set: " + item);
            assertEquals(expectedService.getServiceId(), item.getServiceId(), "Unexpected item service id");
            assertNotEmpty("Item name not set: " + item, item.getName());

            if (item instanceof StreamInfoItem) {
                StreamInfoItem streamInfoItem = (StreamInfoItem) item;
                assertNotEmpty("Uploader name not set: " + item, streamInfoItem.getUploaderName());

                // assertNotEmpty("Uploader url not set: " + item, streamInfoItem.getUploaderUrl());
                final String uploaderUrl = streamInfoItem.getUploaderUrl();
                if (!isNullOrEmpty(uploaderUrl)) {
                    assertIsSecureUrl(uploaderUrl);
                    assertExpectedLinkType(expectedService, uploaderUrl, LinkType.CHANNEL);
                }

                final String uploaderAvatarUrl = streamInfoItem.getUploaderAvatarUrl();
                if (!isNullOrEmpty(uploaderAvatarUrl)) {
                    assertIsSecureUrl(uploaderAvatarUrl);
                }

                assertExpectedLinkType(expectedService, streamInfoItem.getUrl(), LinkType.STREAM);

                if (!isNullOrEmpty(streamInfoItem.getTextualUploadDate())) {
                    final DateWrapper uploadDate = streamInfoItem.getUploadDate();
                    assertNotNull(uploadDate,"No parsed upload date");
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

        assertNotEquals(LinkType.NONE, linkTypeByUrl,
                "Url is not recognized by its own service: \"" + url + "\"");
        assertEquals(expectedLinkType, linkTypeByUrl,
                "Service returned wrong link type for: \"" + url + "\"");
    }

    public static void assertOnlyContainsType(ListExtractor.InfoItemsPage<? extends InfoItem> items, InfoItem.InfoType expectedType) {
        for (InfoItem item : items.getItems()) {
            assertEquals(expectedType, item.getInfoType(),
                    "Item list contains unexpected info types");
        }
    }

    public static <T extends InfoItem> void assertNoMoreItems(ListExtractor<T> extractor) throws Exception {
        final ListExtractor.InfoItemsPage<T> initialPage = extractor.getInitialPage();
        assertFalse(initialPage.hasNextPage(), "More items available when it shouldn't");
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
        assertTrue(initialPage.hasNextPage(), "Doesn't have more items");
        ListExtractor.InfoItemsPage<T> nextPage = extractor.getPage(initialPage.getNextPage());
        final List<T> items = nextPage.getItems();
        assertFalse(items.isEmpty(), "Next page is empty");
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
