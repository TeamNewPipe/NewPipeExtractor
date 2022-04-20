package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;

import javax.annotation.Nullable;

public class InvidiousCommentsInfoItemExtractor implements CommentsInfoItemExtractor {

    private final JsonObject json;
    private final String url;
    private final String baseUrl;

    public InvidiousCommentsInfoItemExtractor(
            final JsonObject json,
            final String url,
            final String baseUrl
    ) {
        this.json = json;
        this.url = url;
        this.baseUrl = baseUrl;
    }

    @Override
    public int getLikeCount() {
        return json.getNumber("likeCount").intValue();
    }

    @Override
    public String getCommentText() {
        return json.getString("content");
    }

    @Override
    public String getTextualUploadDate() {
        return json.getString("publishedText");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() {
        return InvidiousParsingHelper.getUploadDateFromEpochTime(
                json.getNumber("published").longValue());
    }

    @Override
    public String getCommentId() {
        return json.getString("commentId");
    }

    @Override
    public String getUploaderUrl() {
        return baseUrl + json.getString("authorUrl");
    }

    @Override
    public String getUploaderName() {
        return json.getString("author");
    }

    @Override
    public String getUploaderAvatarUrl() {
        return InvidiousParsingHelper.getThumbnailUrl(json.getArray("authorThumbnails"));
    }

    @Override
    public boolean isHeartedByUploader() throws ParsingException {
        return json.has("creatorHeart");
    }

    @Override
    public String getName() throws ParsingException {
        return json.getString("author");
    }

    @Override
    public String getUrl() throws ParsingException {
        return url;
    }

    @Override
    public String getThumbnailUrl() {
        return InvidiousParsingHelper.getThumbnailUrl(json.getArray("authorThumbnails"));
    }

}
