package org.schabi.newpipe.extractor.services;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.List;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.*;

public interface BaseListExtractorTest extends BaseExtractorTest {
    @SuppressWarnings("unused")
    void testRelatedItems() throws Exception;
    @SuppressWarnings("unused")
    void testMoreRelatedItems() throws Exception;


    static void defaultTestListOfItems(int expectedServiceId, List<? extends InfoItem> itemsList, List<Throwable> errors) {
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
            }
        }
    }

    static void defaultTestRelatedItems(ListExtractor extractor, int expectedServiceId) throws Exception {
        final InfoItemsCollector<? extends InfoItem, ?> itemsCollector = extractor.getInfoItems();
        final List<? extends InfoItem> itemsList = itemsCollector.getItemList();
        List<Throwable> errors = itemsCollector.getErrors();

        defaultTestListOfItems(expectedServiceId, itemsList, errors);
    }

    static ListExtractor.InfoItemPage<? extends InfoItem> defaultTestMoreItems(ListExtractor extractor, int expectedServiceId) throws Exception {
        assertTrue("Doesn't have more items", extractor.hasNextPage());
        ListExtractor.InfoItemPage<? extends InfoItem> nextPage = extractor.getPage(extractor.getNextPageUrl());
        assertTrue("Next page is empty", !nextPage.getItemsList().isEmpty());
        assertEmptyErrors("Next page have errors", nextPage.getErrors());

        defaultTestListOfItems(expectedServiceId, nextPage.getItemsList(), nextPage.getErrors());
        return nextPage;
    }

    static void defaultTestGetPageInNewExtractor(ListExtractor extractor, ListExtractor newExtractor, int expectedServiceId) throws Exception {
        final String nextPageUrl = extractor.getNextPageUrl();

        final ListExtractor.InfoItemPage<? extends InfoItem> page = newExtractor.getPage(nextPageUrl);
        BaseListExtractorTest.defaultTestListOfItems(expectedServiceId, page.getItemsList(), page.getErrors());
    }
}
