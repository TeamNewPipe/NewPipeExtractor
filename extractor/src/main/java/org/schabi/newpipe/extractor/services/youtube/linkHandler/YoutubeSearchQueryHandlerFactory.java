package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class YoutubeSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    public static final String CHARSET_UTF_8 = "UTF-8";

    public static final String STREAM = "stream";
    public static final String CHANNEL = "channel";
    public static final String PLAYLIST = "playlist";
    public static final String ANY = "any";

    public static YoutubeSearchQueryHandlerFactory getInstance() {
        return new YoutubeSearchQueryHandlerFactory();
    }

    @Override
    public String getUrl(String searchString, List<String> contentFilters, String sortFilter) throws ParsingException {
        try {
            final String url = "https://www.youtube.com/results"
                    + "?q=" + URLEncoder.encode(searchString, CHARSET_UTF_8);

            if(contentFilters.size() > 0) {
                switch (contentFilters.get(0)) {
                    case STREAM: return url + "&sp=EgIQAVAU";
                    case CHANNEL: return url + "&sp=EgIQAlAU";
                    case PLAYLIST: return url + "&sp=EgIQA1AU";
                    case ANY:
                    default:
                }
            }

            return url;
        } catch (UnsupportedEncodingException e) {
            throw new ParsingException("Could not encode query", e);
        }
    }

    @Override
    public String[] getAvailableContentFilter() {
        return new String[] {
                ANY,
                STREAM,
                CHANNEL,
                PLAYLIST};
    }
}
