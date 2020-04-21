package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;

import javax.annotation.Nullable;

public class SoundcloudCommentsInfoItemExtractor implements CommentsInfoItemExtractor {

    private JsonObject json;
    private String url;

    public SoundcloudCommentsInfoItemExtractor(JsonObject json, String url) {
        this.json = json;
        this.url = url;
    }

    @Override
    public String getCommentId() throws ParsingException {
        return json.getNumber("id").toString();
    }

    @Override
    public String getCommentText() throws ParsingException {
        return json.getString("body");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return json.getObject("user").getString("username");
    }

    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        return json.getObject("user").getString("avatar_url");
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return json.getObject("user").getString("permalink_url");
    }

    @Override
    public String getTextualUploadDate() throws ParsingException {
        return json.getString("created_at");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return new DateWrapper(SoundcloudParsingHelper.parseDateFrom(getTextualUploadDate()));
    }

    @Override
    public int getLikeCount() throws ParsingException {
        return -1;
    }

    @Override
    public String getName() throws ParsingException {
        return json.getObject("user").getString("permalink");
    }

    @Override
    public String getUrl() throws ParsingException {
        return url;
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return json.getObject("user").getString("avatar_url");
    }
}
