package org.schabi.newpipe.extractor.services.media_ccc.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;
import java.util.regex.Pattern;

public final class MediaCCCLiveListLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final MediaCCCLiveListLinkHandlerFactory INSTANCE =
            new MediaCCCLiveListLinkHandlerFactory();

    private static final String STREAM_PATTERN = "^(?:https?://)?media\\.ccc\\.de/live$";

    private MediaCCCLiveListLinkHandlerFactory() {
    }

    public static MediaCCCLiveListLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        return "live";
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        return Pattern.matches(STREAM_PATTERN, url);
    }

    @Override
    public String getUrl(final String id,
                         final List<String> contentFilter,
                         final String sortFilter)
            throws ParsingException, UnsupportedOperationException {
        // FIXME: wrong URL; should be https://streaming.media.ccc.de/{conference_slug}/{room_slug}
        return "https://media.ccc.de/live";
    }
}
