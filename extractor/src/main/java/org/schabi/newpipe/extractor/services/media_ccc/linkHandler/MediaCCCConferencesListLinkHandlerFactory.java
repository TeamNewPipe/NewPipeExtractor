package org.schabi.newpipe.extractor.services.media_ccc.linkHandler;

import org.schabi.newpipe.extractor.search.filter.FilterItem;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MediaCCCConferencesListLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final MediaCCCConferencesListLinkHandlerFactory INSTANCE =
            new MediaCCCConferencesListLinkHandlerFactory();

    private MediaCCCConferencesListLinkHandlerFactory() {
    }

    public static MediaCCCConferencesListLinkHandlerFactory getInstance() {
        return INSTANCE;
    }
    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        return "conferences";
    }

    @Override
    public String getUrl(final String id,
                         @Nonnull final List<FilterItem> contentFilter,
                         @Nullable final List<FilterItem> sortFilter)
            throws ParsingException, UnsupportedOperationException {
        return "https://media.ccc.de/public/conferences";
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        return url.equals("https://media.ccc.de/b/conferences")
                || url.equals("https://media.ccc.de/public/conferences")
                || url.equals("https://api.media.ccc.de/public/conferences");
    }
}
