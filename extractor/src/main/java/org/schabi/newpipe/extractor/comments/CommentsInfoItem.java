package org.schabi.newpipe.extractor.comments;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.Description;

import javax.annotation.Nullable;

public class CommentsInfoItem extends InfoItem {

    private String commentId;
    private Description commentText;
    private String uploaderName;
    private String uploaderAvatarUrl;
    private String uploaderUrl;
    private boolean uploaderVerified;
    private String textualUploadDate;
    @Nullable
    private DateWrapper uploadDate;
    private int likeCount;
    private String textualLikeCount;
    private boolean heartedByUploader;
    private boolean pinned;
    private int streamPosition;
    private int replyCount;
    @Nullable
    private Page replies;

    public static final int NO_LIKE_COUNT = -1;
    public static final int NO_STREAM_POSITION = -1;

    public static final int UNKNOWN_REPLY_COUNT = -1;

    public CommentsInfoItem(final int serviceId, final String url, final String name) {
        super(InfoType.COMMENT, serviceId, url, name);
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(final String commentId) {
        this.commentId = commentId;
    }

    public Description getCommentText() {
        return commentText;
    }

    public void setCommentText(final Description commentText) {
        this.commentText = commentText;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(final String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getUploaderAvatarUrl() {
        return uploaderAvatarUrl;
    }

    public void setUploaderAvatarUrl(final String uploaderAvatarUrl) {
        this.uploaderAvatarUrl = uploaderAvatarUrl;
    }

    public String getUploaderUrl() {
        return uploaderUrl;
    }

    public void setUploaderUrl(final String uploaderUrl) {
        this.uploaderUrl = uploaderUrl;
    }

    public String getTextualUploadDate() {
        return textualUploadDate;
    }

    public void setTextualUploadDate(final String textualUploadDate) {
        this.textualUploadDate = textualUploadDate;
    }

    @Nullable
    public DateWrapper getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(@Nullable final DateWrapper uploadDate) {
        this.uploadDate = uploadDate;
    }

    /**
     * @return the comment's like count
     * or {@link CommentsInfoItem#NO_LIKE_COUNT} if it is unavailable
     */
    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(final int likeCount) {
        this.likeCount = likeCount;
    }

    public String getTextualLikeCount() {
        return textualLikeCount;
    }

    public void setTextualLikeCount(final String textualLikeCount) {
        this.textualLikeCount = textualLikeCount;
    }

    public void setHeartedByUploader(final boolean isHeartedByUploader) {
        this.heartedByUploader = isHeartedByUploader;
    }

    public boolean isHeartedByUploader() {
        return this.heartedByUploader;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(final boolean pinned) {
        this.pinned = pinned;
    }

    public void setUploaderVerified(final boolean uploaderVerified) {
        this.uploaderVerified = uploaderVerified;
    }

    public boolean isUploaderVerified() {
        return uploaderVerified;
    }

    public void setStreamPosition(final int streamPosition) {
        this.streamPosition = streamPosition;
    }

    /**
     * Get the playback position of the stream to which this comment belongs.
     * This is not supported by all services.
     *
     * @return the playback position in seconds or {@link #NO_STREAM_POSITION} if not available
     */
    public int getStreamPosition() {
        return streamPosition;
    }

    public void setReplyCount(final int replyCount) {
        this.replyCount = replyCount;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplies(@Nullable final Page replies) {
        this.replies = replies;
    }

    @Nullable
    public Page getReplies() {
        return this.replies;
    }
}
