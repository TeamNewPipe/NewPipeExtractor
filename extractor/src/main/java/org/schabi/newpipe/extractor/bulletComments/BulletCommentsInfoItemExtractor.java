package org.schabi.newpipe.extractor.bulletComments;

import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public interface BulletCommentsInfoItemExtractor extends InfoItemExtractor {
    @Override
    default String getName() throws ParsingException {
        return null;
    }

    @Override
    default List<org.schabi.newpipe.extractor.Image> getThumbnails() throws ParsingException {
        return Collections.emptyList();
    }

    @Override
    default String getUrl() throws ParsingException {
        return null;
    }

    default String getCommentText() throws ParsingException {
        return "";
    }

    /**
     * Returns ARGB32 int. White: 0xFFFFFFFF.
     * @return ARGB32 int. White: 0xFFFFFFFF.
     */
    default int getArgbColor() throws ParsingException {
        return 0xFFFFFFFF;
    }

    default BulletCommentsInfoItem.Position getPosition() throws ParsingException {
        return BulletCommentsInfoItem.Position.REGULAR;
    }

    default double getRelativeFontSize() throws ParsingException {
        return 0.7;
    }

    default int getLastingTime() {
        return -1;
    }

    default boolean isLive() throws ParsingException {
        return false;
    }

    // Must be implemented. If that is a live stream you should at least
    // calculate the time from the start of the stream.
    Duration getDuration() throws ParsingException;
}
