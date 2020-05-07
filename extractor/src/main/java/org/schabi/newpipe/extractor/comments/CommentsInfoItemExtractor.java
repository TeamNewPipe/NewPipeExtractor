package org.schabi.newpipe.extractor.comments;

import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamExtractor;

import javax.annotation.Nullable;

public interface CommentsInfoItemExtractor extends InfoItemExtractor {

    /**
     * Return the like count of the comment, or -1 if it's unavailable
     * @see StreamExtractor#getLikeCount()
     */
    int getLikeCount() throws ParsingException;

    /**
     * The text of the comment
     */
    String getCommentText() throws ParsingException;

    /**
     * The upload date given by the service, unmodified
     * @see StreamExtractor#getTextualUploadDate()
     */
    String getTextualUploadDate() throws ParsingException;

    /**
     * The upload date wrapped with DateWrapper class
     * @see StreamExtractor#getUploadDate()
     */
    @Nullable
    DateWrapper getUploadDate() throws ParsingException;

    String getCommentId() throws ParsingException;

    String getUploaderUrl() throws ParsingException;

    String getUploaderName() throws ParsingException;

    String getUploaderAvatarUrl() throws ParsingException;
}
