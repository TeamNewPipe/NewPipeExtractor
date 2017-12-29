package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.utils.Parser;

public class YoutubeFeedUrlIdHandler implements UrlIdHandler {

    private static final YoutubeFeedUrlIdHandler instance = new YoutubeFeedUrlIdHandler();
    private static final String ID_PATTERN = "channel_id=([A-Za-z0-9_-]+)";

    public static YoutubeFeedUrlIdHandler getInstance() {
        return instance;
    }

    @Override
    public String getUrl(String id) {
        return "https://www.youtube.com/feeds/videos.xml?channel_id=" + id;
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
        return (url.contains("youtube") || url.contains("youtu.be"))
                && url.contains("videos.xml?channel_id=");
    }
}
