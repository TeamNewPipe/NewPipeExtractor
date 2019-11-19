package org.schabi.newpipe.extractor.comments;

import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;

import javax.annotation.Nullable;

public interface CommentsInfoItemExtractor extends InfoItemExtractor {
    String getCommentId() throws ParsingException;
    String getCommentText() throws ParsingException;
    String getAuthorName() throws ParsingException;
    String getAuthorThumbnail() throws ParsingException;
    String getAuthorEndpoint() throws ParsingException;
    String getTextualPublishedTime() throws ParsingException;
    @Nullable
    DateWrapper getPublishedTime() throws ParsingException;
    int getLikeCount() throws ParsingException;
}
