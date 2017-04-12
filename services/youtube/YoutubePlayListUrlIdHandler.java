package org.schabi.newpipe.extractor.services.youtube;


import org.schabi.newpipe.extractor.Parser;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

public class YoutubePlayListUrlIdHandler implements UrlIdHandler {

    private static final String ID_PATTERN = "([\\-a-zA-Z0-9_]{34})";

    @Override
    public String getUrl(String listId) {
        return "https://www.youtube.com/playlist?list=" + listId;
    }

    @Override
    public String getId(String url) throws ParsingException {
        try {
            return Parser.matchGroup1("list=" + ID_PATTERN, url);
        } catch (final Exception exception) {
            throw new ParsingException("Error could not parse url :" + exception.getMessage(), exception);
        }
    }

    @Override
    public String cleanUrl(String complexUrl) throws ParsingException {
        return getUrl(getId(complexUrl));
    }

    @Override
    public boolean acceptUrl(String videoUrl) {
        final boolean hasNotEmptyUrl = videoUrl != null && !videoUrl.isEmpty();
        final boolean isYoutubeDomain = hasNotEmptyUrl && (videoUrl.contains("youtube") || videoUrl.contains("youtu.be"));
        return isYoutubeDomain && videoUrl.contains("list=");
    }
}
