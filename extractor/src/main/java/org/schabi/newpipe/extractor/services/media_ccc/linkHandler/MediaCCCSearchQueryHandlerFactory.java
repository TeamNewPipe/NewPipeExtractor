package org.schabi.newpipe.extractor.services.media_ccc.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.util.List;

public final class MediaCCCSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    private static final MediaCCCSearchQueryHandlerFactory INSTANCE =
            new MediaCCCSearchQueryHandlerFactory();

    public static final String ALL = "all";
    public static final String CONFERENCES = "conferences";
    public static final String EVENTS = "events";

    private MediaCCCSearchQueryHandlerFactory() {
    }

    public static MediaCCCSearchQueryHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String[] getAvailableContentFilter() {
        return new String[]{
                ALL,
                CONFERENCES,
                EVENTS
        };
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
        try {
            return "https://media.ccc.de/public/events/search?q=" + Utils.encodeUrlUtf8(query);
        } catch (final UnsupportedEncodingException e) {
            throw new ParsingException("Could not create search string with query: " + query, e);
        }
    }
}
