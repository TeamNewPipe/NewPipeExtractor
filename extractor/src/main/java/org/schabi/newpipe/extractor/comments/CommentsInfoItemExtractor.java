package org.schabi.newpipe.extractor.comments;

import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamExtractor;

import javax.annotation.Nullable;

public interface CommentsInfoItemExtractor extends InfoItemExtractor {

    /**
     * AuthorEndpoint, in other words, link to authors' channel page
     */
    String getAuthorEndpoint() throws ParsingException;

    /**
     * Return the like count of the comment, or -1 if it's unavailable
     * see {@link StreamExtractor#getLikeCount()}
     */
    int getLikeCount() throws ParsingException;

    /**
     * The text of the comment
     */
    String getCommentText() throws ParsingException;

    /**
     * The upload date given by the service, unmodified
     * see {@link StreamExtractor#getTextualUploadDate()}
     */
    String getTextualPublishedTime() throws ParsingException;

    /**
     * The upload date wrapped with DateWrapper class
     * see {@link StreamExtractor#getUploadDate()}
     */
    @Nullable
    DateWrapper getPublishedTime() throws ParsingException;

    String getCommentId() throws ParsingException;

    String getAuthorName() throws ParsingException;

    String getAuthorThumbnail() throws ParsingException;
}
