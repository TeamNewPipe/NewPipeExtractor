package org.schabi.newpipe.extractor.services.youtube.extractors;

import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;


public class YoutubeCommentsInfoItemExtractor implements CommentsInfoItemExtractor {

    private final JsonObject json;
    private final String url;

    public YoutubeCommentsInfoItemExtractor(JsonObject json, String url) {
        this.json = json;
        this.url = url;
    }

    @Override
    public String getUrl() throws ParsingException {
        return url;
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        try {
            JsonArray arr = (JsonArray) JsonUtils.getValue(json, "authorThumbnail.thumbnails");
            return (String) JsonUtils.getValue(arr.getObject(2), "url");
        } catch (Exception e) {
            throw new ParsingException("Could not get thumbnail url", e);
        }
    }

    @Override
    public String getName() throws ParsingException {
        try {
            return YoutubeCommentsExtractor.getYoutubeText((JsonObject) JsonUtils.getValue(json, "authorText"));
        } catch (Exception e) {
            throw new ParsingException("Could not get author name", e);
        }
    }

    @Override
    public String getPublishedTime() throws ParsingException {
        try {
            return YoutubeCommentsExtractor.getYoutubeText((JsonObject) JsonUtils.getValue(json, "publishedTimeText"));
        } catch (Exception e) {
            throw new ParsingException("Could not get publishedTimeText", e);
        }
    }

    @Override
    public Integer getLikeCount() throws ParsingException {
        try {
            return (Integer) JsonUtils.getValue(json, "likeCount");
        } catch (Exception e) {
            throw new ParsingException("Could not get like count", e);
        }
    }

    @Override
    public String getCommentText() throws ParsingException {
        try {
            return YoutubeCommentsExtractor.getYoutubeText((JsonObject) JsonUtils.getValue(json, "contentText"));
        } catch (Exception e) {
            throw new ParsingException("Could not get comment text", e);
        }
    }

    @Override
    public String getCommentId() throws ParsingException {
        try {
            return (String) JsonUtils.getValue(json, "commentId");
        } catch (Exception e) {
            throw new ParsingException("Could not get comment id", e);
        }
    }

    @Override
    public String getAuthorThumbnail() throws ParsingException {
        try {
            JsonArray arr = (JsonArray) JsonUtils.getValue(json, "authorThumbnail.thumbnails");
            return (String) JsonUtils.getValue(arr.getObject(2), "url");
        } catch (Exception e) {
            throw new ParsingException("Could not get author thumbnail", e);
        }
    }

    @Override
    public String getAuthorName() throws ParsingException {
        try {
            return YoutubeCommentsExtractor.getYoutubeText((JsonObject) JsonUtils.getValue(json, "authorText"));
        } catch (Exception e) {
            throw new ParsingException("Could not get author name", e);
        }
    }

    @Override
    public String getAuthorEndpoint() throws ParsingException {
        try {
            return "https://youtube.com"
                    + (String) JsonUtils.getValue(json, "authorEndpoint.browseEndpoint.canonicalBaseUrl");
        } catch (Exception e) {
            throw new ParsingException("Could not get author endpoint", e);
        }
    }

}
