package org.schabi.newpipe.extractor.comments;

import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

public interface CommentsInfoItemExtractor extends InfoItemExtractor {
    String getCommentId() throws ParsingException;
    String getCommentText() throws ParsingException;
    String getAuthorName() throws ParsingException;
    String getAuthorThumbnail() throws ParsingException;
    String getAuthorEndpoint() throws ParsingException;
    String getPublishedTime() throws ParsingException;
    Integer getLikeCount() throws ParsingException;
}
