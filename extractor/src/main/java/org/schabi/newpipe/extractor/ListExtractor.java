package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;


/**
 * Base class to extractors that have a list (e.g. playlists, users).
 */
public abstract class ListExtractor<R extends InfoItem> extends Extractor {
    /**
     * Constant that should be returned whenever
     * a list has an unknown number of items.
     */
    public static final long ITEM_COUNT_UNKNOWN = -1;
    /**
     * Constant that should be returned whenever a list has an
     * infinite number of items. For example a YouTube mix.
     */
    public static final long ITEM_COUNT_INFINITE = -2;
    /**
     * Constant that should be returned whenever a list
     * has an unknown number of items bigger than 100.
     */
    public static final long ITEM_COUNT_MORE_THAN_100 = -3;

    public ListExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    /**
     * A {@link InfoItemsPage InfoItemsPage} corresponding to the initial page
     * where the items are from the initial request and the nextPage relative to it.
     *
     * @return a {@link InfoItemsPage} corresponding to the initial page
     */
    @Nonnull
    public abstract InfoItemsPage<R> getInitialPage() throws IOException, ExtractionException;

    /**
     * Get a list of items corresponding to the specific requested page.
     *
     * @param page any page got from the exclusive implementation of the list extractor
     * @return a {@link InfoItemsPage} corresponding to the requested page
     * @see InfoItemsPage#getNextPage()
     */
    public abstract InfoItemsPage<R> getPage(final Page page) throws IOException, ExtractionException;

    @Override
    public ListLinkHandler getLinkHandler() {
        return (ListLinkHandler) super.getLinkHandler();
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Inner
    //////////////////////////////////////////////////////////////////////////*/

    /**
     * A class that is used to wrap a list of gathered items and eventual errors, it
     * also contains a field that points to the next available page ({@link #nextPage}).
     */
    public static class InfoItemsPage<T extends InfoItem> {
        private static final InfoItemsPage<InfoItem> EMPTY =
                new InfoItemsPage<>(Collections.<InfoItem>emptyList(), null, Collections.<Throwable>emptyList());

        /**
         * A convenient method that returns a representation of an empty page.
         *
         * @return a type-safe page with the list of items and errors empty and the nextPage set to {@code null}.
         */
        public static <T extends InfoItem> InfoItemsPage<T> emptyPage() {
            //noinspection unchecked
            return (InfoItemsPage<T>) EMPTY;
        }

        /**
         * The current list of items of this page
         */
        private final List<T> itemsList;

        /**
         * Url pointing to the next page relative to this one
         *
         * @see ListExtractor#getPage(Page)
         * @see Page
         */
        private final Page nextPage;

        /**
         * Errors that happened during the extraction
         */
        private final List<Throwable> errors;

        public InfoItemsPage(InfoItemsCollector<T, ?> collector, Page nextPage) {
            this(collector.getItems(), nextPage, collector.getErrors());
        }

        public InfoItemsPage(List<T> itemsList, Page nextPage, List<Throwable> errors) {
            this.itemsList = itemsList;
            this.nextPage = nextPage;
            this.errors = errors;
        }

        public boolean hasNextPage() {
            return Page.isValid(nextPage);
        }

        public List<T> getItems() {
            return itemsList;
        }

        public Page getNextPage() {
            return nextPage;
        }

        public List<Throwable> getErrors() {
            return errors;
        }
    }
}
