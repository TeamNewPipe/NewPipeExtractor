package org.schabi.newpipe.extractor.services.youtube.youtube;

import org.schabi.newpipe.extractor.services.youtube.YoutubeLikeInstance;

public class YoutubeInstance extends YoutubeLikeInstance<YoutubeDirectService> {

    public static final String SERVICE_NAME = "YouTube";

    public static final YoutubeInstance YOUTUBE =
            new YoutubeInstance("https://youtube.com", SERVICE_NAME);

    protected YoutubeInstance(final String url, final String name) {
        super(url, name);
    }

    @Override
    public YoutubeDirectService getNewStreamingService(final int id) {
        return new YoutubeDirectService(id);
    }
}
