package org.schabi.newpipe.extractor.search;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

public abstract class SearchExtractor extends ListExtractor<InfoItem> {

    public static class NothingFoundException extends ExtractionException {
        public NothingFoundException(String message) {
            super(message);
        }
    }

    private final InfoItemsSearchCollector collector;

    public SearchExtractor(StreamingService service, SearchQuerryUrlHandler urlIdHandler) {
        super(service, urlIdHandler);
        collector = new InfoItemsSearchCollector(service.getServiceId());
    }

    public String getSearchQuerry() {
        return getUrlIdHandler().getSearchQuerry();
    }

    public abstract String getSearchSuggestion() throws ParsingException;
    protected InfoItemsSearchCollector getInfoItemSearchCollector() {
        return collector;
    }

    @Override
    public SearchQuerryUrlHandler getUrlIdHandler() {
        return (SearchQuerryUrlHandler) super.getUrlIdHandler();
    }

}
