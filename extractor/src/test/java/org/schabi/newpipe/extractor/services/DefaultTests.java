package org.schabi.newpipe.extractor.services;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.*;

public final class DefaultTests {
    public static void defaultTestListOfItems(int expectedServiceId, List<? extends InfoItem> itemsList, List<Throwable> errors) {
        assertTrue("List of items is empty", !itemsList.isEmpty());
        assertFalse("List of items contains a null element", itemsList.contains(null));
        assertEmptyErrors("Errors during stream list extraction", errors);

        for (InfoItem item : itemsList) {
            assertIsSecureUrl(item.getUrl());
            if (item.getThumbnailUrl() != null && !item.getThumbnailUrl().isEmpty()) {
                assertIsSecureUrl(item.getThumbnailUrl());
            }
            assertNotNull("InfoItem type not set: " + item, item.getInfoType());
            assertEquals("Service id doesn't match: " + item, expectedServiceId, item.getServiceId());

            if (item instanceof StreamInfoItem) {
                StreamInfoItem streamInfoItem = (StreamInfoItem) item;
                assertNotEmpty("Uploader name not set: " + item, streamInfoItem.getUploaderName());
                assertNotEmpty("Uploader url not set: " + item, streamInfoItem.getUploaderUrl());

                final String textualUploadDate = streamInfoItem.getTextualUploadDate();
                if (textualUploadDate != null && !textualUploadDate.isEmpty()) {
                    final DateWrapper uploadDate = streamInfoItem.getUploadDate();
                    assertNotNull("No parsed upload date", uploadDate);
                    assertTrue("Upload date not in the past", uploadDate.date().before(Calendar.getInstance()));
                }

            }
        }
    }

    public static <T extends InfoItem> ListExtractor.InfoItemsPage<T> defaultTestRelatedItems(ListExtractor<T> extractor, int expectedServiceId) throws Exception {
        final ListExtractor.InfoItemsPage<T> page = extractor.getInitialPage();
        final List<T> itemsList = page.getItems();
        List<Throwable> errors = page.getErrors();

        defaultTestListOfItems(expectedServiceId, itemsList, errors);
        return page;
    }

    public static <T extends InfoItem> ListExtractor.InfoItemsPage<T> defaultTestMoreItems(ListExtractor<T> extractor, int expectedServiceId) throws Exception {
        assertTrue("Doesn't have more items", extractor.hasNextPage());
        ListExtractor.InfoItemsPage<T> nextPage = extractor.getPage(extractor.getNextPageUrl());
        final List<T> items = nextPage.getItems();
        assertTrue("Next page is empty", !items.isEmpty());
        assertEmptyErrors("Next page have errors", nextPage.getErrors());

        defaultTestListOfItems(expectedServiceId, nextPage.getItems(), nextPage.getErrors());
        return nextPage;
    }

    public static void defaultTestGetPageInNewExtractor(ListExtractor<? extends InfoItem> extractor, ListExtractor<? extends InfoItem> newExtractor, int expectedServiceId) throws Exception {
        final String nextPageUrl = extractor.getNextPageUrl();

        final ListExtractor.InfoItemsPage<? extends InfoItem> page = newExtractor.getPage(nextPageUrl);
        defaultTestListOfItems(expectedServiceId, page.getItems(), page.getErrors());
    }
}
