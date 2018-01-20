package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Base class to extractors that have a list (e.g. playlists, users).
 */
public abstract class ListExtractor extends Extractor {
    protected String nextStreamsUrl;

    /**
     * Get a new ListExtractor with the given nextStreamsUrl set.
     */
    public ListExtractor(StreamingService service, String url, String nextStreamsUrl) throws ExtractionException {
        super(service, url);
        setNextStreamsUrl(nextStreamsUrl);
    }

    @Nonnull
    public abstract StreamInfoItemCollector getStreams() throws IOException, ExtractionException;

    public abstract NextItemsResult getNextStreams() throws IOException, ExtractionException;

    public boolean hasMoreStreams() {
        return nextStreamsUrl != null && !nextStreamsUrl.isEmpty();
    }

    public String getNextStreamsUrl() {
        return nextStreamsUrl;
    }

    public void setNextStreamsUrl(String nextStreamsUrl) {
        this.nextStreamsUrl = nextStreamsUrl;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Inner
    //////////////////////////////////////////////////////////////////////////*/

    public static class NextItemsResult {
        /**
         * The current list of items to this result
         */
        public final List<InfoItem> nextItemsList;

        /**
         * Next url to fetch more items
         */
        public final String nextItemsUrl;

        /**
         * Errors that happened during the extraction
         */
        public final List<Throwable> errors;

        public NextItemsResult(InfoItemCollector collector, String nextItemsUrl) {
            this(collector.getItemList(), nextItemsUrl, collector.getErrors());
        }

        public NextItemsResult(List<InfoItem> nextItemsList, String nextItemsUrl, List<Throwable> errors) {
            this.nextItemsList = nextItemsList;
            this.nextItemsUrl = nextItemsUrl;
            this.errors = errors;
        }

        public boolean hasMoreStreams() {
            return nextItemsUrl != null && !nextItemsUrl.isEmpty();
        }

        public List<InfoItem> getNextItemsList() {
            return nextItemsList;
        }

        public String getNextItemsUrl() {
            return nextItemsUrl;
        }

        public List<Throwable> getErrors() {
            return errors;
        }
    }

}
