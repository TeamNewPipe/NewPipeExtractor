package org.schabi.newpipe.extractor.services.youtube.retrofit.model;

import org.schabi.newpipe.extractor.services.youtube.retrofit.service.YoutubeRetrofitService;

public class YoutubeClient {
    private final String hl = "en-GB";
    private final String gl = "GB";
    private final String clientName;
    private final String clientVersion;

    public YoutubeClient() {
        this("WEB", YoutubeRetrofitService.HARDCODED_CLIENT_VERSION);
    }

    protected YoutubeClient(final String clientName, final String clientVersion) {
        this.clientName = clientName;
        this.clientVersion = clientVersion;
    }

    @Override
    public String toString() {
        return "YoutubeClient{hl='" + hl + "'"
                + ", gl='" + gl + '\''
                + ", clientName='" + clientName + "'"
                + ", clientVersion='" + clientVersion + "'}";
    }
}
