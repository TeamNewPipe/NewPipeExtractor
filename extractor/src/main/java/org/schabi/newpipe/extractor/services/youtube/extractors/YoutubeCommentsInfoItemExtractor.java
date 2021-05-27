package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.utils.Utils.EMPTY_STRING;

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
            final JsonArray arr = JsonUtils.getArray(json, "authorThumbnail.thumbnails");
            return JsonUtils.getString(arr.getObject(2), "url");
        } catch (Exception e) {
            throw new ParsingException("Could not get thumbnail url", e);
        }
    }

    @Override
    public String getName() throws ParsingException {
        try {
            return getTextFromObject(JsonUtils.getObject(json, "authorText"));
        } catch (Exception e) {
            return EMPTY_STRING;
        }
    }

    @Override
    public String getTextualUploadDate() throws ParsingException {
        try {
            return getTextFromObject(JsonUtils.getObject(json, "publishedTimeText"));
        } catch (Exception e) {
            throw new ParsingException("Could not get publishedTimeText", e);
        }
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        String textualPublishedTime = getTextualUploadDate();
        if (timeAgoParser != null && textualPublishedTime != null && !textualPublishedTime.isEmpty()) {
            return timeAgoParser.parse(textualPublishedTime);
        } else {
            return null;
        }
    }

    /**
     * @implNote The method is parsing internally a localized string.<br/>
     * <ul>
     *     <li>
     *         More than >1k likes will result in an inaccurate number
     *     </li>
     *     <li>
     *         This will fail for other languages than English.
     *         However as long as the Extractor only uses "en-GB"
     *         (as seen in {@link org.schabi.newpipe.extractor.services.youtube.YoutubeService#SUPPORTED_LANGUAGES})
     *         everything will work fine.
     *      </li>
     * </ul>
     * <br/>
     * Consider using {@link #getTextualLikeCount()}
     */
    @Override
    public int getLikeCount() throws ParsingException {
        json.getInt("");
        // This may return a language dependent version, e.g. in German: 3,3 Mio
        final String textualLikeCount = getTextualLikeCount();
        try {
            if (Utils.isBlank(textualLikeCount)) {
                return 0;
            }

            return (int) Utils.mixedNumberWordToLong(textualLikeCount);
        } catch (Exception e) {
            throw new ParsingException("Unexpected error while converting textual like count to like count", e);
        }
    }

    @Override
    public String getTextualLikeCount() throws ParsingException {
        /*
         * Example results as of 2021-05-20:
         * Language = English
         * 3.3M
         * 48K
         * 1.4K
         * 270K
         * 19
         * 6
         *
         * Language = German
         * 3,3 Mio
         * 48.189
         * 1419
         * 270.984
         * 19
         * 6
         */
        try {
            // If a comment has no likes voteCount is not set
            if (!json.has("voteCount")) {
                return EMPTY_STRING;
            }

            final JsonObject voteCountObj = JsonUtils.getObject(json, "voteCount");
            if (voteCountObj.isEmpty()) {
                return EMPTY_STRING;
            }
            return getTextFromObject(voteCountObj);
        } catch (Exception e) {
            throw new ParsingException("Could not get vote count", e);
        }
    }

    @Override
    public String getCommentText() throws ParsingException {
        try {
            final JsonObject contentText = JsonUtils.getObject(json, "contentText");
            if (contentText.isEmpty()) {
                // completely empty comments as described in
                // https://github.com/TeamNewPipe/NewPipeExtractor/issues/380#issuecomment-668808584
                return EMPTY_STRING;
            }
            final String commentText = getTextFromObject(contentText);
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
    public String getUploaderAvatarUrl() throws ParsingException {
        try {
            JsonArray arr = JsonUtils.getArray(json, "authorThumbnail.thumbnails");
            return JsonUtils.getString(arr.getObject(2), "url");
        } catch (Exception e) {
            throw new ParsingException("Could not get author thumbnail", e);
        }
    }

    @Override
    public boolean isHeartedByUploader() throws ParsingException {
        return json.has("creatorHeart");
    }

    @Override
    public boolean isPinned() {
        return json.has("pinnedCommentBadge");
    }

    public boolean isUploaderVerified() throws ParsingException {
        // impossible to get this information from the mobile layout
        return false;
    }

    @Override
    public String getUploaderName() throws ParsingException {
        try {
            return getTextFromObject(JsonUtils.getObject(json, "authorText"));
        } catch (Exception e) {
            return EMPTY_STRING;
        }
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        try {
            return "https://youtube.com/channel/" + JsonUtils.getString(json, "authorEndpoint.browseEndpoint.browseId");
        } catch (Exception e) {
            return EMPTY_STRING;
        }
    }

}
