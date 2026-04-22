package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.bulletComments.BulletCommentsInfoItem;
import org.schabi.newpipe.extractor.bulletComments.BulletCommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.time.Duration;

public class YoutubeBulletCommentsInfoItemExtractor implements BulletCommentsInfoItemExtractor {
    private final JsonObject data;
    private long startTime;
    private long offsetDuration; // the expected offset of the comment from the start of the video
    public YoutubeBulletCommentsInfoItemExtractor(final JsonObject item,
                                                   final long startTime,
                                                   final long offsetDuration) {
        data = item;
        this.startTime = startTime;
        this.offsetDuration = offsetDuration;
    }

    @Override
    public String getCommentText() throws ParsingException {
        final JsonArray array = data.getObject("message").getArray("runs");
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < array.size(); i++) {
            if (array.getObject(i).has("text")) {
                result.append(array.getObject(i).getString("text"));
            }
        }
        return result.toString().replaceAll("□", "");
    }

    @Override
    public int getArgbColor() throws ParsingException {
        return BulletCommentsInfoItemExtractor.super.getArgbColor();
    }

    @Override
    public BulletCommentsInfoItem.Position getPosition() throws ParsingException {
        return BulletCommentsInfoItem.Position.REGULAR;
    }

    @Override
    public double getRelativeFontSize() throws ParsingException {
        return BulletCommentsInfoItemExtractor.super.getRelativeFontSize();
    }

    @Override
    public Duration getDuration() throws ParsingException {
        // return Duration.ofMillis(
        //         Long.parseLong(data.getString("timestampUsec")) / 1000 - startTime);
        return offsetDuration == -1 ? Duration.ZERO : Duration.ofMillis(offsetDuration);
    }

    @Override
    public boolean isLive() throws ParsingException {
        return true;
    }
}
