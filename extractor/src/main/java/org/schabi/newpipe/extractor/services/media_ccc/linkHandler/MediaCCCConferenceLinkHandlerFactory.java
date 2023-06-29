package org.schabi.newpipe.extractor.services.media_ccc.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

import java.util.List;

public final class MediaCCCConferenceLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final MediaCCCConferenceLinkHandlerFactory INSTANCE
            = new MediaCCCConferenceLinkHandlerFactory();

    public static final String CONFERENCE_API_ENDPOINT
            = "https://api.media.ccc.de/public/conferences/";
    public static final String CONFERENCE_PATH = "https://media.ccc.de/c/";
    private static final String ID_PATTERN
            = "(?:(?:(?:api\\.)?media\\.ccc\\.de/public/conferences/)"
            + "|(?:media\\.ccc\\.de/[bc]/))([^/?&#]*)";

    private MediaCCCConferenceLinkHandlerFactory() {
    }

    public static MediaCCCConferenceLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getUrl(final String id,
                         final List<String> contentFilter,
                         final String sortFilter)
            throws ParsingException, UnsupportedOperationException {
        return CONFERENCE_PATH + id;
    }

    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        return Parser.matchGroup1(ID_PATTERN, url);
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        try {
            return getId(url) != null;
        } catch (final ParsingException e) {
            return false;
        }
    }
}
