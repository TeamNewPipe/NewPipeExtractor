package org.schabi.newpipe.extractor.services.youtube.shared.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.shared.YoutubePlaylistHelper;
import org.schabi.newpipe.extractor.services.youtube.shared.YoutubeUrlHelper;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;

public abstract class YoutubeLikePlaylistLinkHandlerFactory extends ListLinkHandlerFactory {

    @Override
    public String getId(final String url) throws ParsingException {
        try {
            final URL urlObj = Utils.stringToURL(url);

            if (!Utils.isHTTP(urlObj) || !(YoutubeUrlHelper.isYoutubeURL(urlObj)
                    || YoutubeUrlHelper.isInvidioURL(urlObj))) {
                throw new ParsingException("The url given is not a YouTube-URL");
            }

            final String path = urlObj.getPath();
            if (!path.equals("/watch") && !path.equals("/playlist")) {
                throw new ParsingException("The url given is neither a video nor a playlist URL");
            }

            final String listID = Utils.getQueryValue(urlObj, "list");

            if (listID == null) {
                throw new ParsingException("The URL given does not include a playlist");
            }

            if (!listID.matches("[a-zA-Z0-9_-]{10,}")) {
                throw new ParsingException(
                        "The list-ID given in the URL does not match the list pattern");
            }

            if (YoutubePlaylistHelper.isYoutubeChannelMixId(listID)
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
     * Returns the url for the mix-playlist.
     * <br/>
     * Example for Youtube: {@code https://youtube.com/watch?v=videoId&list=playlistId}
     *
     * @param videoID id of the video
     * @param listID  id of the list
     * @return url for the mix-playlist
     */
    protected abstract String getMixUrl(String videoID, String listID);

    /**
     * If it is a mix (auto-generated playlist) URL, return a {@link LinkHandler} where the URL is
     * like see {@link #getMixUrl(String, String)}
     * <p>Otherwise use super</p>
     */
    @Override
    public ListLinkHandler fromUrl(final String url) throws ParsingException {
        try {
            final URL urlObj = Utils.stringToURL(url);
            final String listID = Utils.getQueryValue(urlObj, "list");
            if (listID != null && YoutubePlaylistHelper.isYoutubeMixId(listID)) {
                String videoID = Utils.getQueryValue(urlObj, "v");
                if (videoID == null) {
                    videoID = YoutubePlaylistHelper.extractVideoIdFromMixId(listID);
                }
                final String newUrl = getMixUrl(videoID, listID);
                return new ListLinkHandler(new LinkHandler(url, newUrl, listID));
            }
        } catch (final MalformedURLException exception) {
            throw new ParsingException("Error could not parse URL: " + exception.getMessage(),
                exception);
        }
        return super.fromUrl(url);
    }
}
