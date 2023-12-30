package org.schabi.newpipe.extractor.services.media_ccc.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.media_ccc.search.filter.MediaCCCFilters;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MediaCCCSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    private static final MediaCCCSearchQueryHandlerFactory INSTANCE =
            new MediaCCCSearchQueryHandlerFactory();

    private MediaCCCSearchQueryHandlerFactory() {
        super(new MediaCCCFilters());
    }

    public static MediaCCCSearchQueryHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getUrl(final String query,
                         @Nonnull final List<FilterItem> contentFilter,
                         @Nullable final List<FilterItem> sortFilter)
            throws ParsingException, UnsupportedOperationException {
        try {
            return "https://media.ccc.de/public/events/search?q=" + Utils.encodeUrlUtf8(query);
        } catch (final UnsupportedEncodingException e) {
            throw new ParsingException("Could not create search string with query: " + query, e);
        }
    }
}
