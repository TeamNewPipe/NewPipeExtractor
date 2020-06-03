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

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
