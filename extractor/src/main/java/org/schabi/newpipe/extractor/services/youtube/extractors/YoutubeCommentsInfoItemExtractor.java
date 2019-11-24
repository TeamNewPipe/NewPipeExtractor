package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nullable;

public class YoutubeCommentsInfoItemExtractor implements CommentsInfoItemExtractor {

    private final JsonObject json;
    private final String url;
    private final TimeAgoParser timeAgoParser;

    public YoutubeCommentsInfoItemExtractor(JsonObject json, String url, TimeAgoParser timeAgoParser) {
        this.json = json;
        this.url = url;
        this.timeAgoParser = timeAgoParser;
    }

    @Override
    public String getUrl() throws ParsingException {
        return url;
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        try {
            JsonArray arr = JsonUtils.getArray(json, "authorThumbnail.thumbnails");
            return JsonUtils.getString(arr.getObject(2), "url");
        } catch (Exception e) {
            throw new ParsingException("Could not get thumbnail url", e);
        }
    }

    @Override
    public String getName() throws ParsingException {
        try {
            return YoutubeCommentsExtractor.getYoutubeText(JsonUtils.getObject(json, "authorText"));
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getTextualPublishedTime() throws ParsingException {
        try {
            return YoutubeCommentsExtractor.getYoutubeText(JsonUtils.getObject(json, "publishedTimeText"));
        } catch (Exception e) {
            throw new ParsingException("Could not get publishedTimeText", e);
        }
    }

    @Nullable
    @Override
    public DateWrapper getPublishedTime() throws ParsingException {
        String textualPublishedTime = getTextualPublishedTime();
        if (timeAgoParser != null && textualPublishedTime != null && !textualPublishedTime.isEmpty()) {
            return timeAgoParser.parse(textualPublishedTime);
        } else {
            return null;
        }
    }

    @Override
    public int getLikeCount() throws ParsingException {
        try {
            return JsonUtils.getNumber(json, "likeCount").intValue();
        } catch (Exception e) {
            throw new ParsingException("Could not get like count", e);
        }
    }

    @Override
    public String getCommentText() throws ParsingException {
        try {
            String commentText = YoutubeCommentsExtractor.getYoutubeText(JsonUtils.getObject(json, "contentText"));
            // youtube adds U+FEFF in some comments. eg. https://www.youtube.com/watch?v=Nj4F63E59io<feff>
            return Utils.removeUTF8BOM(commentText);
        } catch (Exception e) {
            throw new ParsingException("Could not get comment text", e);
        }
    }

    @Override
    public String getCommentId() throws ParsingException {
        try {
            return JsonUtils.getString(json, "commentId");
        } catch (Exception e) {
            throw new ParsingException("Could not get comment id", e);
        }
    }

    @Override
    public String getAuthorThumbnail() throws ParsingException {
        try {
            JsonArray arr = JsonUtils.getArray(json, "authorThumbnail.thumbnails");
            return JsonUtils.getString(arr.getObject(2), "url");
        } catch (Exception e) {
            throw new ParsingException("Could not get author thumbnail", e);
        }
    }

    @Override
    public String getAuthorName() throws ParsingException {
        try {
            return YoutubeCommentsExtractor.getYoutubeText(JsonUtils.getObject(json, "authorText"));
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getAuthorEndpoint() throws ParsingException {
        try {
            return "https://youtube.com/channel/" + JsonUtils.getString(json, "authorEndpoint.browseEndpoint.browseId");
        } catch (Exception e) {
            return "";
        }
    }

}
