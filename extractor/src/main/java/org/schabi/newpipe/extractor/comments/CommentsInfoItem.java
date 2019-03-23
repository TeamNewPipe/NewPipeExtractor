package org.schabi.newpipe.extractor.comments;

import org.schabi.newpipe.extractor.InfoItem;

public class CommentsInfoItem extends InfoItem{

	private String commentId;
	private String commentText;
	private String authorName;
	private String authorThumbnail;
	private String authorEndpoint;
	private String publishedTime;
	private Integer likeCount;
	
	public CommentsInfoItem(int serviceId, String url, String name) {
		super(InfoType.COMMENT, serviceId, url, name);
		// TODO Auto-generated constructor stub
	}
	
	public String getCommentText() {
		return commentText;
	}

	public void setCommentText(String contentText) {
		this.commentText = contentText;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getAuthorThumbnail() {
		return authorThumbnail;
	}

	public void setAuthorThumbnail(String authorThumbnail) {
		this.authorThumbnail = authorThumbnail;
	}

	public String getAuthorEndpoint() {
		return authorEndpoint;
	}

	public void setAuthorEndpoint(String authorEndpoint) {
		this.authorEndpoint = authorEndpoint;
	}

	public String getPublishedTime() {
		return publishedTime;
	}

	public void setPublishedTime(String publishedTime) {
		this.publishedTime = publishedTime;
	}

	public Integer getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}

	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}

}
