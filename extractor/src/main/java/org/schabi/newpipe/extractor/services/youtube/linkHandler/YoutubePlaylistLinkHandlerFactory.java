package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.URL;
import java.util.List;

public class YoutubePlaylistLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final YoutubePlaylistLinkHandlerFactory instance = new YoutubePlaylistLinkHandlerFactory();

    public static YoutubePlaylistLinkHandlerFactory getInstance() {
        return instance;
    }

    @Override
    public String getUrl(String id, List<String> contentFilters, String sortFilter) {
        return "https://www.youtube.com/playlist?list=" + id;
    }

    @Override
    public String getId(String url) throws ParsingException {
        try {
            URL urlObj = Utils.stringToURL(url);

            if (!Utils.isHTTP(urlObj) || !(YoutubeParsingHelper.isYoutubeURL(urlObj)
                    || YoutubeParsingHelper.isInvidioURL(urlObj))) {
                throw new ParsingException("the url given is not a Youtube-URL");
            }

            String path = urlObj.getPath();
            if (!path.equals("/watch") && !path.equals("/playlist")) {
                throw new ParsingException("the url given is neither a video nor a playlist URL");
            }

            String listID = Utils.getQueryValue(urlObj, "list");

            if (listID == null) {
                throw new ParsingException("the url given does not include a playlist");
            }

            if (!listID.matches("[a-zA-Z0-9_-]{10,}")) {
                throw new ParsingException("the list-ID given in the URL does not match the list pattern");
            }

            // Don't accept auto-generated "Mix" playlists but auto-generated YouTube Music playlists
            if (listID.startsWith("RD") && !listID.startsWith("RDCLAK")) {
                throw new ContentNotSupportedException("YouTube Mix playlists are not yet supported");
            }

            return listID;
        } catch (final Exception exception) {
            throw new ParsingException("Error could not parse url :" + exception.getMessage(), exception);
        }
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        try {
            getId(url);
        } catch (ParsingException e) {
            return false;
        }
        return true;
    }
}
