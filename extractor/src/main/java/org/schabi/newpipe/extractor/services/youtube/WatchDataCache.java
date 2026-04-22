package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.stream.StreamType;

public class WatchDataCache {
    public StreamType streamType;
    public long startAt;
    public boolean shouldBeLive = true;
    public String currentUrl;
    // save all the 4 last status
    public StreamType lastStreamType;
    public long lastStartAt;
    public boolean lastShouldBeLive = true;
    public String lastCurrentUrl;
    // use the url to instance the extractor, then save the current data to lasts
    public void init(final String url) {
        if (url.equals(currentUrl)) {
            return;
        }
        lastStreamType = streamType;
        lastStartAt = startAt;
        lastShouldBeLive = shouldBeLive;
        lastCurrentUrl = currentUrl;
        currentUrl = url;
    }

}
