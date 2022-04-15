package org.schabi.newpipe.extractor.services.youtube.shared.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.shared.YoutubeUrlHelper;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.URL;
import java.util.regex.Pattern;

public abstract class YoutubeLikeChannelLinkHandlerFactory extends ListLinkHandlerFactory {

    protected static final Pattern EXCLUDED_SEGMENTS =
            Pattern.compile("playlist|watch|attribution_link|watch_popup|embed|feed|select_site");

    /**
     * Returns true if path conform to
     * custom short channel URLs like youtube.com/yourcustomname
     *
     * @param splitPath path segments array
     * @return true - if value conform to short channel URL, false - not
     */
    protected boolean isCustomShortChannelUrl(final String[] splitPath) {
        return splitPath.length == 1 && !EXCLUDED_SEGMENTS.matcher(splitPath[0]).matches();
    }

    @Override
    public String getId(final String url) throws ParsingException {
        try {
            final URL urlObj = Utils.stringToURL(url);
            String path = urlObj.getPath();

            if (!Utils.isHTTP(urlObj) || !(YoutubeUrlHelper.isYoutubeURL(urlObj)
                    || YoutubeUrlHelper.isInvidioURL(urlObj)
                    || YoutubeUrlHelper.isHooktubeURL(urlObj))) {
                throw new ParsingException("the URL given is not a Youtube-URL");
            }

            // remove leading "/"
            path = path.substring(1);
            String[] splitPath = path.split("/");

            // Handle custom short channel URLs like youtube.com/yourcustomname
            if (isCustomShortChannelUrl(splitPath)) {
                path = "c/" + path;
                splitPath = path.split("/");
            }

            if (!path.startsWith("user/")
                    && !path.startsWith("channel/")
                    && !path.startsWith("c/")) {
                throw new ParsingException("the URL given is neither a channel nor an user");
            }

            final String id = splitPath[1];

            if (id == null || !id.matches("[A-Za-z0-9_-]+")) {
                throw new ParsingException("The given id is not a Youtube-Channel-ID");
            }

            return splitPath[0] + "/" + id;
        } catch (final Exception exception) {
            throw new ParsingException("Error could not parse url :" + exception.getMessage(),
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
}
