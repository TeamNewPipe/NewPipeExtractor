package org.schabi.newpipe.extractor.services.youtube.retrofit.model;

import org.schabi.newpipe.extractor.services.youtube.retrofit.service.YoutubeMusicRetrofitService;

import java.util.Collections;
import java.util.List;

public class YoutubeMusicClient extends YoutubeClient {
    private final List<String> experimentIds = Collections.emptyList();
    private final String experimentToken = "";
    private final Object locationInfo = new Object();
    private final Object musicAppInfo = new Object();

    public YoutubeMusicClient() {
        super("WEB_REMIX", YoutubeMusicRetrofitService.HARDCODED_CLIENT_VERSION);
    }

    @Override
    public String toString() {
        return "YoutubeMusicClient{experimentIds=" + experimentIds
                + ", experimentToken='" + experimentToken + "'"
                + ", locationInfo=" + locationInfo
                + ", musicAppInfo=" + musicAppInfo + "} " + super.toString();
    }
}
