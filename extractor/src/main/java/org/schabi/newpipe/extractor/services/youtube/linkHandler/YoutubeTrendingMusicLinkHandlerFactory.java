package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public final class YoutubeTrendingMusicLinkHandlerFactory
        extends ListLinkHandlerFactory {

    public static final String KIOSK_ID = "trending_music";

    public static final YoutubeTrendingMusicLinkHandlerFactory INSTANCE =
            new YoutubeTrendingMusicLinkHandlerFactory();

    private static final String PATH = "/charts/TrendingVideos";

    private YoutubeTrendingMusicLinkHandlerFactory() {
    }

    @Override
    public String getUrl(final String id,
                         final List<String> contentFilter,
                         final String sortFilter)
            throws ParsingException, UnsupportedOperationException {
        return "https://charts.youtube.com" + PATH + "/RightNow";
    }

    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        return KIOSK_ID;
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        final URL urlObj;
        try {
            urlObj = Utils.stringToURL(url);
        } catch (final MalformedURLException e) {
            return false;
        }

        return Utils.isHTTP(urlObj)
                && "charts.youtube.com".equals(urlObj.getHost().toLowerCase(Locale.ROOT))
                // Accept URLs not containing the /RightNow part
                && urlObj.getPath().startsWith(PATH);
    }
}
