package org.schabi.newpipe.extractor.services.youtube.extractors;

import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

public class YoutubeCommentsInfoItemExtractor implements CommentsInfoItemExtractor{
    
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
        JsonArray arr = JsonUtils.getValue(json, "authorThumbnail.thumbnails");
        return JsonUtils.getValue(arr.getObject(2), "url");
    }

    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getValue(json, "authorText.simpleText");
    }

    @Override
    public String getPublishedTime() throws ParsingException {
        JsonArray arr = JsonUtils.getValue(json, "publishedTimeText.runs");
        return JsonUtils.getValue(arr.getObject(0), "text");
    }

    @Override
    public Integer getLikeCount() throws ParsingException {
        return JsonUtils.getValue(json, "likeCount");
    }

    @Override
    public String getCommentText() throws ParsingException {
        try {
            return JsonUtils.getValue(json, "contentText.simpleText");
        } catch (Exception e) {
            JsonArray arr = JsonUtils.getValue(json, "contentText.runs");
            return JsonUtils.getValue(arr.getObject(0), "text");
        }
    }

    @Override
    public String getCommentId() throws ParsingException {
        return JsonUtils.getValue(json, "commentId");
    }

    @Override
    public String getAuthorThumbnail() throws ParsingException {
        JsonArray arr = JsonUtils.getValue(json, "authorThumbnail.thumbnails");
        return JsonUtils.getValue(arr.getObject(2), "url");
    }

    @Override
    public String getAuthorName() throws ParsingException {
        return JsonUtils.getValue(json, "authorText.simpleText");
    }

    @Override
    public String getAuthorEndpoint() throws ParsingException {
            return "https://youtube.com" + JsonUtils.getValue(json, "authorEndpoint.browseEndpoint.canonicalBaseUrl");
    }

}
