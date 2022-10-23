package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class YoutubeSearchLinkHandlerFactory extends ListLinkHandlerFactory {
    private static final YoutubeSearchLinkHandlerFactory INSTANCE
            = new YoutubeSearchLinkHandlerFactory();

    private YoutubeSearchLinkHandlerFactory() {}

    public static YoutubeSearchLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getUrl(final String id, final List<String> contentFilters,
                         final String sortFilter) {
        return "https://www.youtube.com/results?search_query=" + id;
    }

    @Override
    public String getId(String url) throws ParsingException {
        try {
            final URL urlObj = Utils.stringToURL(url);

            if (!Utils.isHTTP(urlObj) || !(YoutubeParsingHelper.isYoutubeURL(urlObj)
                    || YoutubeParsingHelper.isInvidioURL(urlObj))) {
                throw new ParsingException("the url given is not a YouTube-URL");
            }

            final String listID = Utils.getQueryValue(urlObj, "search_query");

            if (listID == null) {
                throw new ParsingException("the URL given does not include a playlist");
            }

            return listID;

        } catch (final Exception exception) {
            throw new ParsingException("Error could not parse URL: " + exception.getMessage(),
                    exception);
        }
    }

    @Override
    public boolean onAcceptUrl(String url) throws ParsingException {
        try {
            getId(url);
        } catch (final ParsingException e) {
            return false;
        }
        return true;
    }
}
