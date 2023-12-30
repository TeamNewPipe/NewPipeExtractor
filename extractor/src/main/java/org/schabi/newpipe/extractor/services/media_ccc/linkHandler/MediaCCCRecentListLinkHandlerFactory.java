package org.schabi.newpipe.extractor.services.media_ccc.linkHandler;

import org.schabi.newpipe.extractor.search.filter.FilterItem;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MediaCCCRecentListLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final MediaCCCRecentListLinkHandlerFactory INSTANCE =
            new MediaCCCRecentListLinkHandlerFactory();
    private static final String PATTERN = "^(https?://)?media\\.ccc\\.de/recent/?$";

    private MediaCCCRecentListLinkHandlerFactory() {
    }

    public static MediaCCCRecentListLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        return "recent";
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        return Pattern.matches(PATTERN, url);
    }

    @Override
    public String getUrl(final String id,
                         @Nonnull final List<FilterItem> contentFilter,
                         @Nullable final List<FilterItem> sortFilter)
            throws ParsingException, UnsupportedOperationException {
        return "https://media.ccc.de/recent";
    }
}
