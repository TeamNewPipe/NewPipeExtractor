package org.schabi.newpipe.extractor.services.soundcloud.linkHandler;

import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

import java.util.List;

public class SoundcloudChartsLinkHandlerFactory extends ListLinkHandlerFactory {
    private final String TOP_URL_PATTERN = "^https?://(www\\.|m\\.)?soundcloud.com/charts(/top)?/?([#?].*)?$";
    private final String URL_PATTERN = "^https?://(www\\.|m\\.)?soundcloud.com/charts(/top|/new)?/?([#?].*)?$";


    @Override
    public String getId(String url) {
        if (Parser.isMatch(TOP_URL_PATTERN, url.toLowerCase())) {
            return "Top 50";
        } else {
            return "New & hot";
        }
    }

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) {
        if (id.equals("Top 50")) {
            return "https://soundcloud.com/charts/top";
        } else {
            return "https://soundcloud.com/charts/new";
        }
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        return Parser.isMatch(URL_PATTERN, url.toLowerCase());
    }
}
