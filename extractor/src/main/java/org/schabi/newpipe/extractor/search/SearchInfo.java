package org.schabi.newpipe.extractor.search;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListInfo;
import org.schabi.newpipe.extractor.ListUrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

public class SearchInfo extends ListInfo<InfoItem> {

    private String searchString = "";
    private String searchSuggestion = "";


    public SearchInfo(int serviceId,
                      ListUrlIdHandler urlIdHandler,
                      String searchString) throws ParsingException {
        super(serviceId, urlIdHandler, "Search");
        this.searchString = searchString;
    }


    public static SearchInfo getInfo(SearchExtractor extractor) throws ExtractionException {
        final SearchInfo info = new SearchInfo(
                extractor.getServiceId(),
                extractor.getUrlIdHandler(),
                extractor.getSearchString());

        try {
            info.searchSuggestion = extractor.getSearchSuggestion();
        } catch (Exception e) {
            info.addError(e);
        }

        return info;
    }

    // Getter
    public String getSearchString() {
        return searchString;
    }

    public String getSearchSuggestion() {
        return searchSuggestion;
    }
}
