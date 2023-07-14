package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class YoutubePlaylistLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final YoutubePlaylistLinkHandlerFactory INSTANCE =
            new YoutubePlaylistLinkHandlerFactory();

    private YoutubePlaylistLinkHandlerFactory() {
    }

    public static YoutubePlaylistLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getUrl(final String id, final List<String> contentFilters,
                         final String sortFilter)
            throws ParsingException, UnsupportedOperationException {
        return "https://www.youtube.com/playlist?list=" + id;
    }

    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        try {
            final URL urlObj = Utils.stringToURL(url);

            if (!Utils.isHTTP(urlObj) || !(YoutubeParsingHelper.isYoutubeURL(urlObj)
                    || YoutubeParsingHelper.isInvidiousURL(urlObj))) {
                throw new ParsingException("the url given is not a YouTube-URL");
            }

            final String path = urlObj.getPath();
            if (!path.equals("/watch") && !path.equals("/playlist")) {
                throw new ParsingException("the url given is neither a video nor a playlist URL");
            }

            final String listID = Utils.getQueryValue(urlObj, "list");

            if (listID == null) {
                throw new ParsingException("the URL given does not include a playlist");
            }

            if (!listID.matches("[a-zA-Z0-9_-]{10,}")) {
                throw new ParsingException(
                        "the list-ID given in the URL does not match the list pattern");
            }

            if (YoutubeParsingHelper.isYoutubeChannelMixId(listID)
                    && Utils.getQueryValue(urlObj, "v") == null) {
                // Video id can't be determined from the channel mix id.
                // See YoutubeParsingHelper#extractVideoIdFromMixId
                throw new ContentNotSupportedException(
                        "Channel Mix without a video id are not supported");
            }

            return listID;
        } catch (final Exception exception) {
            throw new ParsingException("Error could not parse URL: " + exception.getMessage(),
                    exception);
        }
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        try {
            getId(url);
        } catch (final ParsingException e) {
            return false;
        }
        return true;
    }

    /**
     * If it is a mix (auto-generated playlist) URL, return a {@link LinkHandler} where the URL is
     * like {@code https://youtube.com/watch?v=videoId&list=playlistId}
     * <p>Otherwise use super</p>
     */
    @Override
    public ListLinkHandler fromUrl(final String url) throws ParsingException {
        try {
            final URL urlObj = Utils.stringToURL(url);
            final String listID = Utils.getQueryValue(urlObj, "list");
            if (listID != null && YoutubeParsingHelper.isYoutubeMixId(listID)) {
                String videoID = Utils.getQueryValue(urlObj, "v");
                if (videoID == null) {
                    videoID = YoutubeParsingHelper.extractVideoIdFromMixId(listID);
                }
                final String newUrl = "https://www.youtube.com/watch?v=" + videoID
                    + "&list=" + listID;
                return new ListLinkHandler(new LinkHandler(url, newUrl, listID));
            }
        } catch (final MalformedURLException exception) {
            throw new ParsingException("Error could not parse URL: " + exception.getMessage(),
                exception);
        }
        return super.fromUrl(url);
    }
}
