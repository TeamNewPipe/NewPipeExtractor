package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.services.youtube.YoutubeService;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService;

/*
 * Created by the-scrabi on 18.02.17.
 */

class ServiceList {
    public static final StreamingService[] serviceList = {
            new YoutubeService(0),
            new SoundcloudService(1)
    };
}
