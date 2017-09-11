package org.schabi.newpipe.extractor.services.soundcloud;

import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.utils.Parser;

public class SoundcloudChartsUrlIdHandler implements UrlIdHandler {
    public String getUrl(String id) {
        if (id.equals("Top 50")) {
            return "https://soundcloud.com/charts/top";
        } else {
            return "https://soundcloud.com/charts/new";
        }
    }

    @Override
    public String getId(String url) {
        if (Parser.isMatch("^https?://(www\\.)?soundcloud.com/charts(/top)?/?([#?].*)?$", url.toLowerCase())) {
            return "Top 50";
        } else {
            return "New & hot";
        }
    }

    @Override
    public String cleanUrl(String url) {
        if (Parser.isMatch("^https?://(www\\.)?soundcloud.com/charts(/top)?/?([#?].*)?$", url.toLowerCase())) {
            return "https://soundcloud.com/charts/top";
        } else {
            return "https://soundcloud.com/charts/new";
        }
    }

    @Override
    public boolean acceptUrl(String url) {
        return Parser.isMatch("^https?://(www\\.)?soundcloud.com/charts(/top|/new)?/?([#?].*)?$", url.toLowerCase());
    }
}
