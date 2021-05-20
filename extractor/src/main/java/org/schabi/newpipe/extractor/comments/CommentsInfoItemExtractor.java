package org.schabi.newpipe.extractor.comments;

import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nullable;

public interface CommentsInfoItemExtractor extends InfoItemExtractor {
    /**
     * The formatted text (e.g. 420, 4K, 4.2M) of the votes
     *
     * May be language dependent
     */
    default String getTextualVoteCount() throws ParsingException {
        return Utils.EMPTY_STRING;
    }

    /**
     * The text of the comment
     */
    String getCommentText() throws ParsingException;

    /**
     * The upload date given by the service, unmodified
     *
     * @see StreamExtractor#getTextualUploadDate()
     */
    String getTextualUploadDate() throws ParsingException;

    /**
     * The upload date wrapped with DateWrapper class
     *
     * @see StreamExtractor#getUploadDate()
     */
    @Nullable
    DateWrapper getUploadDate() throws ParsingException;

    String getCommentId() throws ParsingException;

    String getUploaderUrl() throws ParsingException;

    String getUploaderName() throws ParsingException;

    String getUploaderAvatarUrl() throws ParsingException;

    /**
     * Whether the comment has been hearted by the uploader
     */
    boolean isHeartedByUploader() throws ParsingException;

    /**
     * Whether the comment is pinned
     */
    boolean isPinned() throws ParsingException;

    /**
     * Whether the uploader is verified by the service
     */
    boolean isUploaderVerified() throws ParsingException;
}
