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

    public SearchExtractor(StreamingService service, SearchQueryHandler linkHandler) {
        super(service, linkHandler);
    }

    public String getSearchString() {
        return getLinkHandler().getSearchString();
    }

    /**
     * The search suggestion provided by the service.
     * <p>
     * This method also returns the corrected query if
     * {@link SearchExtractor#isCorrectedSearch()} is true.
     *
     * @return a suggestion to another query, the corrected query, or an empty String.
     * @throws ParsingException
     */
    @Nonnull
    public abstract String getSearchSuggestion() throws ParsingException;

    @Override
    public SearchQueryHandler getLinkHandler() {
        return (SearchQueryHandler) super.getLinkHandler();
    }

    @Nonnull
    @Override
    public String getName() {
        return getLinkHandler().getSearchString();
    }

    /**
     * Tell if the search was corrected by the service (if it's not exactly the search you typed).
     * <p>
     * Example: on YouTube, if you search for "pewdeipie",
     * it will give you results for "pewdiepie", then isCorrectedSearch should return true.
     *
     * @return whether the results comes from a corrected query or not.
     */
    public abstract boolean isCorrectedSearch() throws ParsingException;
}
