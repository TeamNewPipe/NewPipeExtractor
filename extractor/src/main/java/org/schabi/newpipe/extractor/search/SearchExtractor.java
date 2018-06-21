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
    private final String contentCountry;

    public SearchExtractor(StreamingService service, SearchQIHFactory urlIdHandler, String contentCountry) {
        super(service, urlIdHandler);
        collector = new InfoItemsSearchCollector(service.getServiceId());
        this.contentCountry = contentCountry;
    }

    public String getSearchString() {
        return getUIHFactory().getSearchString();
    }

    public abstract String getSearchSuggestion() throws ParsingException;

    protected InfoItemsSearchCollector getInfoItemSearchCollector() {
        return collector;
    }

    @Override
    public SearchQIHFactory getUIHFactory() {
        return (SearchQIHFactory) super.getUIHFactory();
    }

    @Override
    public String getName() {
        return getUIHFactory().getSearchString();
    }

    protected String getContentCountry() {
        return contentCountry;
    }
}
