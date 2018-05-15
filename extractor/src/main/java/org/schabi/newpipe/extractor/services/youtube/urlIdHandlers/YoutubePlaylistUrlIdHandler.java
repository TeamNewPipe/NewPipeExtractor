package org.schabi.newpipe.extractor.services.youtube.urlIdHandlers;


import org.schabi.newpipe.extractor.ListUrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.Parser;

public class YoutubePlaylistUrlIdHandler extends ListUrlIdHandler {

    private static final YoutubePlaylistUrlIdHandler instance = new YoutubePlaylistUrlIdHandler();
    private static final String ID_PATTERN = "([\\-a-zA-Z0-9_]{10,})";

    public static YoutubePlaylistUrlIdHandler getInstance() {
        return instance;
    }

    @Override
    public String getUrl() {
        return "https://www.youtube.com/playlist?list=" + id;
    }

    @Override
    public String onGetIdFromUrl(String url) throws ParsingException {
        try {
            return Parser.matchGroup1("list=" + ID_PATTERN, url);
        } catch (final Exception exception) {
            throw new ParsingException("Error could not parse url :" + exception.getMessage(), exception);
        }
    }


    @Override
    public boolean onAcceptUrl(final String url) {
        final boolean hasNotEmptyUrl = url != null && !url.isEmpty();
        final boolean isYoutubeDomain = hasNotEmptyUrl && (url.contains("youtube") || url.contains("youtu.be"));
        return isYoutubeDomain && url.contains("list=");
    }
}
