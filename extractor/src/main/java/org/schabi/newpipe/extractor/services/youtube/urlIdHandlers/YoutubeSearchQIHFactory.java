package org.schabi.newpipe.extractor.services.youtube.urlIdHandlers;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.uih.SearchQIHFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class YoutubeSearchQIHFactory extends SearchQIHFactory {

    public static final String CHARSET_UTF_8 = "UTF-8";

    public static final String STREAM = "stream";
    public static final String CHANNEL = "channel";
    public static final String PLAYLIST = "playlist";
    public static final String ANY = "any";

    public static YoutubeSearchQIHFactory getInstance() {
        return new YoutubeSearchQIHFactory();
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
                STREAM,
                CHANNEL,
                PLAYLIST,
                ANY};
    }
}
