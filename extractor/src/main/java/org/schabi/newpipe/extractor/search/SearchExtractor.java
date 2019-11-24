package org.schabi.newpipe.extractor.search;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;

import javax.annotation.Nonnull;

public abstract class SearchExtractor extends ListExtractor<InfoItem> {

    public static class NothingFoundException extends ExtractionException {
        public NothingFoundException(String message) {
            super(message);
        }
    }

    private final InfoItemsSearchCollector collector;

    public SearchExtractor(StreamingService service, SearchQueryHandler linkHandler) {
        super(service, linkHandler);
        collector = new InfoItemsSearchCollector(service.getServiceId());
    }

    public String getSearchString() {
        return getLinkHandler().getSearchString();
    }

    public abstract String getSearchSuggestion() throws ParsingException;

    protected InfoItemsSearchCollector getInfoItemSearchCollector() {
        return collector;
    }

    @Override
    public SearchQueryHandler getLinkHandler() {
        return (SearchQueryHandler) super.getLinkHandler();
    }

    @Nonnull
    @Override
    public String getName() {
        return getLinkHandler().getSearchString();
    }
}
