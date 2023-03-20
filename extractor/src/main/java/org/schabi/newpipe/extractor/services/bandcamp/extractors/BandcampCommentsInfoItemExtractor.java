package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImageUrl;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.Description;

public class BandcampCommentsInfoItemExtractor implements CommentsInfoItemExtractor {

    private final JsonObject review;
    private final String url;

    public BandcampCommentsInfoItemExtractor(final JsonObject review, final String url) {
        this.review = review;
        this.url = url;
    }

    @Override
    public String getName() throws ParsingException {
        return getCommentText().getContent();
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return getUploaderAvatarUrl();
    }

    @Override
    public Description getCommentText() throws ParsingException {
        return new Description(review.getString("why"), Description.PLAIN_TEXT);
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return review.getString("name");
    }

    @Override
    public String getUploaderAvatarUrl() {
        return getImageUrl(review.getLong("image_id"), false);
    }
}
