package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Base class to extractors that have a list (e.g. playlists, users).
 */
public abstract class ListExtractor extends Extractor {

    /**
     * Get a new ListExtractor with the given nextPageUrl set.
     */
    public ListExtractor(StreamingService service, String url) {
        super(service, url);
    }

    @Nonnull
    public abstract InfoItemsCollector getInfoItems() throws IOException, ExtractionException;

    public abstract String getNextPageUrl() throws IOException, ExtractionException;

    public abstract InfoItemPage getPage(final String nextPageUrl) throws IOException, ExtractionException;

    public boolean hasNextPage() throws IOException, ExtractionException {
        return getNextPageUrl() != null && !getNextPageUrl().isEmpty();
    }



    /*//////////////////////////////////////////////////////////////////////////
    // Inner
    //////////////////////////////////////////////////////////////////////////*/

    public static class InfoItemPage {
        /**
         * The current list of items to this result
         */
        public final List<InfoItem> infoItemList;

        /**
         * Next url to fetch more items
         */
        public final String nextPageUrl;

        /**
         * Errors that happened during the extraction
         */
        public final List<Throwable> errors;

        public InfoItemPage(InfoItemsCollector collector, String nextPageUrl) {
            this(collector.getItemList(), nextPageUrl, collector.getErrors());
        }

        public InfoItemPage(List<InfoItem> infoItemList, String nextPageUrl, List<Throwable> errors) {
            this.infoItemList = infoItemList;
            this.nextPageUrl = nextPageUrl;
            this.errors = errors;
        }

        public boolean hasNextPage() {
            return nextPageUrl != null && !nextPageUrl.isEmpty();
        }

        public List<InfoItem> getItemsList() {
            return infoItemList;
        }

        public String getNextPageUrl() {
            return nextPageUrl;
        }

        public List<Throwable> getErrors() {
            return errors;
        }
    }

}
