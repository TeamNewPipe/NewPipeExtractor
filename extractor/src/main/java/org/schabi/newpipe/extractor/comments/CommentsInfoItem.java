package org.schabi.newpipe.extractor.comments;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.localization.DateWrapper;

import java.util.List;

import javax.annotation.Nullable;

public class CommentsInfoItem extends InfoItem {

    private String commentId;
    private String commentText;
    private String authorName;
    private List<Image> authorThumbnails;
    private String authorEndpoint;
    private String textualPublishedTime;
    @Nullable private DateWrapper publishedTime;
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

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public List<Image> getAuthorThumbnails() {
        return authorThumbnails;
    }

    public void setAuthorThumbnails(List<Image> authorThumbnails) {
        this.authorThumbnails = authorThumbnails;
    }

    public String getAuthorEndpoint() {
        return authorEndpoint;
    }

    public void setAuthorEndpoint(String authorEndpoint) {
        this.authorEndpoint = authorEndpoint;
    }

    public String getTextualPublishedTime() {
        return textualPublishedTime;
    }

    public void setTextualPublishedTime(String textualPublishedTime) {
        this.textualPublishedTime = textualPublishedTime;
    }

    @Nullable
    public DateWrapper getPublishedTime() {
        return publishedTime;
    }

    public void setPublishedTime(@Nullable DateWrapper publishedTime) {
        this.publishedTime = publishedTime;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
