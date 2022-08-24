package org.schabi.newpipe.extractor.comments;

import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeCommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;

import javax.annotation.Nullable;

public interface CommentsInfoItemExtractor extends InfoItemExtractor {

    /**
     * Return the like count of the comment,
     * or {@link CommentsInfoItem#NO_LIKE_COUNT} if it is unavailable.
     *
     * <br>
     *
     * NOTE: Currently only implemented for YT {@link
     * YoutubeCommentsInfoItemExtractor#getLikeCount()}
     * with limitations (only approximate like count is returned)
     *
     * @see StreamExtractor#getLikeCount()
     * @return the comment's like count
     * or {@link CommentsInfoItem#NO_LIKE_COUNT} if it is unavailable
     */
    default int getLikeCount() throws ParsingException {
        return CommentsInfoItem.NO_LIKE_COUNT;
    }

    /**
     * The unmodified like count given by the service
     * <br>
     * It may be language dependent
     */
    default String getTextualLikeCount() throws ParsingException {
        return "";
    }

    /**
     * The text of the comment
     */
    default String getCommentText() throws ParsingException {
        return "";
    }

    /**
     * The upload date given by the service, unmodified
     *
     * @see StreamExtractor#getTextualUploadDate()
     */
    default String getTextualUploadDate() throws ParsingException {
        return "";
    }

    /**
     * The upload date wrapped with DateWrapper class
     *
     * @see StreamExtractor#getUploadDate()
     */
    @Nullable
    default DateWrapper getUploadDate() throws ParsingException {
        return null;
    }

    default String getCommentId() throws ParsingException {
        return "";
    }

    default String getUploaderUrl() throws ParsingException {
        return "";
    }

    default String getUploaderName() throws ParsingException {
        return "";
    }

    default String getUploaderAvatarUrl() throws ParsingException {
        return "";
    }

    /**
     * Whether the comment has been hearted by the uploader
     */
    default boolean isHeartedByUploader() throws ParsingException {
        return false;
    }

    /**
     * Whether the comment is pinned
     */
    default boolean isPinned() throws ParsingException {
        return false;
    }

    /**
     * Whether the uploader is verified by the service
     */
    default boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    /**
     * The playback position of the stream to which this comment belongs.
     * @see CommentsInfoItem#getStreamPosition()
     */
    default int getStreamPosition() throws ParsingException {
        return CommentsInfoItem.NO_STREAM_POSITION;
    }

    /**
     * The continuation page which is used to get comment replies from.
     * @return the continuation Page for the replies, or null if replies are not supported
     */
    @Nullable
    default Page getReplies() throws ParsingException {
        return null;
    }
}
