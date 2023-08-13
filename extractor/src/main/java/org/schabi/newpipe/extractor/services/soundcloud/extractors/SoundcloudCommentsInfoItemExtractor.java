package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.Description;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.parseDateFrom;

public class SoundcloudCommentsInfoItemExtractor implements CommentsInfoItemExtractor {
    private final JsonObject json;
    private final String url;

    public SoundcloudCommentsInfoItemExtractor(final JsonObject json, final String url) {
        this.json = json;
        this.url = url;
    }

    @Override
    public String getCommentId() {
        return Objects.toString(json.getLong("id"), null);
    }

    @Override
    public Description getCommentText() {
        return new Description(json.getString("body"), Description.PLAIN_TEXT);
    }

    @Override
    public String getUploaderName() {
        return json.getObject("user").getString("username");
    }

    @Nonnull
    @Override
    public List<Image> getUploaderAvatars() {
        return getAllImagesFromArtworkOrAvatarUrl(json.getObject("user").getString("avatar_url"));
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return json.getObject("user").getBoolean("verified");
    }

    @Override
    public int getStreamPosition() {
        return json.getInt("timestamp") / 1000; // convert milliseconds to seconds
    }

    @Override
    public String getUploaderUrl() {
        return json.getObject("user").getString("permalink_url");
    }

    @Override
    public String getTextualUploadDate() {
        return json.getString("created_at");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return new DateWrapper(parseDateFrom(getTextualUploadDate()));
    }

    @Override
    public String getName() throws ParsingException {
        return json.getObject("user").getString("permalink");
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() {
        return getAllImagesFromArtworkOrAvatarUrl(json.getObject("user").getString("avatar_url"));
    }
}
