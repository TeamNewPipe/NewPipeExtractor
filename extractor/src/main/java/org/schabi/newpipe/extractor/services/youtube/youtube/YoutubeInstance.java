package org.schabi.newpipe.extractor.services.youtube.youtube;

import org.schabi.newpipe.extractor.services.youtube.YoutubeLikeInstance;

public class YoutubeInstance extends YoutubeLikeInstance<YoutubeDirectService> {

    public static final YoutubeInstance YOUTUBE =
            new YoutubeInstance("https://youtube.com", "YouTube");

    protected YoutubeInstance(final String url, final String name) {
        super(url, name);
    }

    @Override
    public YoutubeDirectService getNewStreamingService(final int id) {
        return new YoutubeDirectService(id);
    }
}
