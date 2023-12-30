package org.schabi.newpipe.extractor.services.media_ccc.linkHandler;

import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

import java.util.List;

/**
 * Since MediaCCC does not really have channel tabs (i.e. it only has one single "tab" with videos),
 * this link handler acts both as the channel link handler and the channel tab link handler. That's
 * why {@link #getAvailableContentFilter()} has been overridden.
 */
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

    /**
     * @see MediaCCCConferenceLinkHandlerFactory
     * @return MediaCCC's only channel "tab", i.e. {@link ChannelTabs#VIDEOS}
     */
    @Override
    public String[] getAvailableContentFilter() {
        return new String[]{
                ChannelTabs.VIDEOS,
        };
    }
}
