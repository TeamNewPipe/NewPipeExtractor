package org.schabi.newpipe.extractor.comments;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.localization.DateWrapper;

import javax.annotation.Nullable;

public class CommentsInfoItem extends InfoItem {

    private String commentId;
    private String commentText;
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

    public static final int NO_LIKE_COUNT = -1;
    public static final int NO_STREAM_POSITION = -1;

    public CommentsInfoItem(int serviceId, String url, String name) {
        super(InfoType.COMMENT, serviceId, url, name);
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getUploaderAvatarUrl() {
        return uploaderAvatarUrl;
    }

    public void setUploaderAvatarUrl(String uploaderAvatarUrl) {
        this.uploaderAvatarUrl = uploaderAvatarUrl;
    }

    public String getUploaderUrl() {
        return uploaderUrl;
    }

    public void setUploaderUrl(String uploaderUrl) {
        this.uploaderUrl = uploaderUrl;
    }

    public String getTextualUploadDate() {
        return textualUploadDate;
    }

    public void setTextualUploadDate(String textualUploadDate) {
        this.textualUploadDate = textualUploadDate;
    }

    @Nullable
    public DateWrapper getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(@Nullable DateWrapper uploadDate) {
        this.uploadDate = uploadDate;
    }

    /**
     * @return the comment's like count
     *         or {@link CommentsInfoItem#NO_LIKE_COUNT} if it is unavailable
     */
    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getTextualLikeCount() {
        return textualLikeCount;
    }

    public void setTextualLikeCount(String textualLikeCount) {
        this.textualLikeCount = textualLikeCount;
    }

    public void setHeartedByUploader(boolean isHeartedByUploader) {
        this.heartedByUploader = isHeartedByUploader;
    }

    public boolean isHeartedByUploader() {
        return this.heartedByUploader;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public void setUploaderVerified(boolean uploaderVerified) {
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
     * @return the playback position in seconds or {@link #NO_STREAM_POSITION} if not available
     */
    public int getStreamPosition() {
        return streamPosition;
    }
}
