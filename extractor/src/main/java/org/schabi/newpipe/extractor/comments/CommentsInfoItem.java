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
    private String textualUploadDate;
    @Nullable
    private DateWrapper uploadDate;
    private int likeCount;

    public CommentsInfoItem(final int serviceId, final String url, final String name) {
        super(InfoType.COMMENT, serviceId, url, name);
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(final String commentId) {
        this.commentId = commentId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(final String commentText) {
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

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(final int likeCount) {
        this.likeCount = likeCount;
    }
}
