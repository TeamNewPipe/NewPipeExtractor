package org.schabi.newpipe.extractor.services.darkibox.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;

import java.util.List;

/**
 * Darkibox does not support search. This is a minimal stub required by the service framework.
 */
public final class DarkiboxSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    private static final DarkiboxSearchQueryHandlerFactory INSTANCE
            = new DarkiboxSearchQueryHandlerFactory();

    private DarkiboxSearchQueryHandlerFactory() {
    }

    public static DarkiboxSearchQueryHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String[] getAvailableContentFilter() {
        return new String[0];
    }

    @Override
    public String[] getAvailableSortFilter() {
        return new String[0];
    }

    @Override
    public String getUrl(final String query,
                         final List<String> contentFilter,
                         final String sortFilter)
            throws ParsingException, UnsupportedOperationException {
        // Darkibox does not support search
        return "https://darkibox.com/";
    }
}
