package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Base class to extractors that have a list (e.g. playlists, users).
 */
public abstract class ListExtractor extends Extractor {

    public ListExtractor(StreamingService service, String url) {
        super(service, url);
    }

    @Nonnull
    public abstract InfoItemsCollector<? extends InfoItem, ?> getInfoItems() throws IOException, ExtractionException;
    public abstract String getNextPageUrl() throws IOException, ExtractionException;
    public abstract InfoItemPage<? extends InfoItem> getPage(final String nextPageUrl) throws IOException, ExtractionException;

    public boolean hasNextPage() throws IOException, ExtractionException {
        final String nextPageUrl = getNextPageUrl();
        return nextPageUrl != null && !nextPageUrl.isEmpty();
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Inner
    //////////////////////////////////////////////////////////////////////////*/

    public static class InfoItemPage<T extends InfoItem> {
        /**
         * The current list of items to this result
         */
        private final List<T> itemsList;

        /**
         * Next url to fetch more items
         */
        private final String nextPageUrl;

        /**
         * Errors that happened during the extraction
         */
        private final List<Throwable> errors;

        public InfoItemPage(InfoItemsCollector<T, ?> collector, String nextPageUrl) {
            this(collector.getItemList(), nextPageUrl, collector.getErrors());
        }

        public InfoItemPage(List<T> itemsList, String nextPageUrl, List<Throwable> errors) {
            this.itemsList = itemsList;
            this.nextPageUrl = nextPageUrl;
            this.errors = errors;
        }

        public boolean hasNextPage() {
            return nextPageUrl != null && !nextPageUrl.isEmpty();
        }

        public List<T> getItemsList() {
            return itemsList;
        }

        public String getNextPageUrl() {
            return nextPageUrl;
        }

        public List<Throwable> getErrors() {
            return errors;
        }
    }

}
