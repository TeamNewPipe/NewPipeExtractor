package org.schabi.newpipe.extractor.services.soundcloud;

import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.utils.Parser;

public class SoundcloudFeedUrlIdHandler implements UrlIdHandler {

    private static final SoundcloudFeedUrlIdHandler instance = new SoundcloudFeedUrlIdHandler();
    private static final String ID_PATTERN = "users:(\\d+)";

    public static SoundcloudFeedUrlIdHandler getInstance() {
        return instance;
    }

    @Override
    public String getUrl(String id) {
        return "https://feeds.soundcloud.com/users/soundcloud:users:" + id + "/sounds.rss";
    }

    @Override
    public String getId(String url) throws Parser.RegexException {
        return Parser.matchGroup1(ID_PATTERN, url);
    }

    @Override
    public String cleanUrl(String complexUrl) throws Parser.RegexException {
        return getUrl(getId(complexUrl));
    }

    @Override
    public boolean acceptUrl(String url) {
        return url.contains("feeds.soundcloud.com") && url.contains("sounds.rss");
    }

}
