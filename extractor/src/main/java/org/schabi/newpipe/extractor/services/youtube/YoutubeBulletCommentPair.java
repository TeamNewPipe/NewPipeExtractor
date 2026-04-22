package org.schabi.newpipe.extractor.services.youtube;

import com.grack.nanojson.JsonObject;

public class YoutubeBulletCommentPair {
    private final JsonObject data;
    // the expected offset of the comment from the start of the video
    private final long offsetDuration;
    public YoutubeBulletCommentPair(final JsonObject item, final long offsetDuration) {
        this.offsetDuration = offsetDuration;
        this.data = item;
    }

    public JsonObject getData() {
        return data;
    }

    public long getOffsetDuration() {
        return offsetDuration;
    }
}
