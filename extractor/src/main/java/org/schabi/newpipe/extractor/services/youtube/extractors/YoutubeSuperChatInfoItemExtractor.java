package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.bulletComments.BulletCommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

public class YoutubeSuperChatInfoItemExtractor extends YoutubeBulletCommentsInfoItemExtractor {
    private JsonObject data;
    public YoutubeSuperChatInfoItemExtractor(final JsonObject item, final long startTime,
                                             final long offsetDuration) {
        super(item, startTime, offsetDuration);
        data = item;
    }

    @Override
    public String getCommentText() throws ParsingException {
        final String superResult = super.getCommentText();
        if (superResult.length() == 0) {
            return "";
        }
        return String.format("(%s) ", data.getObject("purchaseAmountText")
                .getString("simpleText")) + super.getCommentText();
    }

    @Override
    public int getArgbColor() throws ParsingException {
        return (int) data.getLong("bodyBackgroundColor");
    }

    @Override
    public BulletCommentsInfoItem.Position getPosition() throws ParsingException {
        return BulletCommentsInfoItem.Position.SUPERCHAT;
    }
}
