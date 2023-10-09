package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.schabi.newpipe.extractor.comments.CommentsInfoItem.UNKNOWN_REPLY_COUNT;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;

public class YoutubeCommentsInfoItemExtractor implements CommentsInfoItemExtractor {

    private final JsonObject json;
    private JsonObject commentRenderer;
    private final String url;
    private final TimeAgoParser timeAgoParser;

    public YoutubeCommentsInfoItemExtractor(final JsonObject json,
                                            final String url,
                                            final TimeAgoParser timeAgoParser) {
        this.json = json;
        this.url = url;
        this.timeAgoParser = timeAgoParser;
    }

    private JsonObject getCommentRenderer() throws ParsingException {
        if (commentRenderer == null) {
            if (json.has("comment")) {
                commentRenderer = JsonUtils.getObject(json, "comment.commentRenderer");
            } else {
                commentRenderer = json;
            }
        }
        return commentRenderer;
    }

    @Nonnull
    private List<Image> getAuthorThumbnails() throws ParsingException {
        try {
            return getImagesFromThumbnailsArray(JsonUtils.getArray(getCommentRenderer(),
                    "authorThumbnail.thumbnails"));
        } catch (final Exception e) {
            throw new ParsingException("Could not get author thumbnails", e);
        }
    }

    @Override
    public String getUrl() throws ParsingException {
        return url;
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        return getAuthorThumbnails();
    }

    @Override
    public String getName() throws ParsingException {
        try {
            return getTextFromObject(JsonUtils.getObject(getCommentRenderer(), "authorText"));
        } catch (final Exception e) {
            return "";
        }
    }

    @Override
    public String getTextualUploadDate() throws ParsingException {
        try {
            return getTextFromObject(JsonUtils.getObject(getCommentRenderer(),
                    "publishedTimeText"));
        } catch (final Exception e) {
            throw new ParsingException("Could not get publishedTimeText", e);
        }
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        final String textualPublishedTime = getTextualUploadDate();
        if (timeAgoParser != null && textualPublishedTime != null
                && !textualPublishedTime.isEmpty()) {
            return timeAgoParser.parse(textualPublishedTime);
        } else {
            return null;
        }
    }

    /**
     * @implNote The method tries first to get the exact like count by using the accessibility data
     * returned. But if the parsing of this accessibility data fails, the method parses internally
     * a localized string.
     * <br>
     * <ul>
     *     <li>More than 1k likes will result in an inaccurate number</li>
     *     <li>This will fail for other languages than English. However as long as the Extractor
     *     only uses "en-GB" (as seen in {@link
     *     org.schabi.newpipe.extractor.services.youtube.YoutubeService#getSupportedLocalizations})
     *     , everything will work fine.</li>
     * </ul>
     * <br>
     * Consider using {@link #getTextualLikeCount()}
     */
    @Override
    public int getLikeCount() throws ParsingException {
        // Try first to get the exact like count by using the accessibility data
        final String likeCount;
        try {
            likeCount = Utils.removeNonDigitCharacters(JsonUtils.getString(getCommentRenderer(),
                    "actionButtons.commentActionButtonsRenderer.likeButton.toggleButtonRenderer"
                            + ".accessibilityData.accessibilityData.label"));
        } catch (final Exception e) {
            // Use the approximate like count returned into the voteCount object
            // This may return a language dependent version, e.g. in German: 3,3 Mio
            final String textualLikeCount = getTextualLikeCount();
            try {
                if (Utils.isBlank(textualLikeCount)) {
                    return 0;
                }

                return (int) Utils.mixedNumberWordToLong(textualLikeCount);
            } catch (final Exception i) {
                throw new ParsingException(
                        "Unexpected error while converting textual like count to like count", i);
            }
        }

        try {
            if (Utils.isBlank(likeCount)) {
                return 0;
            }

            return Integer.parseInt(likeCount);
        } catch (final Exception e) {
            throw new ParsingException("Unexpected error while parsing like count as Integer", e);
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
            if (!getCommentRenderer().has("voteCount")) {
                return "";
            }

            final JsonObject voteCountObj = JsonUtils.getObject(getCommentRenderer(), "voteCount");
            if (voteCountObj.isEmpty()) {
                return "";
            }
            return getTextFromObject(voteCountObj);
        } catch (final Exception e) {
            throw new ParsingException("Could not get the vote count", e);
        }
    }

    @Override
    public Description getCommentText() throws ParsingException {
        try {
            final JsonObject contentText = JsonUtils.getObject(getCommentRenderer(), "contentText");
            if (contentText.isEmpty()) {
                // completely empty comments as described in
                // https://github.com/TeamNewPipe/NewPipeExtractor/issues/380#issuecomment-668808584
                return Description.EMPTY_DESCRIPTION;
            }
            final String commentText = getTextFromObject(contentText, true);
            // YouTube adds U+FEFF in some comments.
            // eg. https://www.youtube.com/watch?v=Nj4F63E59io<feff>
            final String commentTextBomRemoved = Utils.removeUTF8BOM(commentText);

            return new Description(commentTextBomRemoved, Description.HTML);
        } catch (final Exception e) {
            throw new ParsingException("Could not get comment text", e);
        }
    }

    @Override
    public String getCommentId() throws ParsingException {
        try {
            return JsonUtils.getString(getCommentRenderer(), "commentId");
        } catch (final Exception e) {
            throw new ParsingException("Could not get comment id", e);
        }
    }

    @Nonnull
    @Override
    public List<Image> getUploaderAvatars() throws ParsingException {
        return getAuthorThumbnails();
    }

    @Override
    public boolean isHeartedByUploader() throws ParsingException {
        final JsonObject commentActionButtonsRenderer = getCommentRenderer()
                .getObject("actionButtons")
                .getObject("commentActionButtonsRenderer");
        return commentActionButtonsRenderer.has("creatorHeart");
    }

    @Override
    public boolean isPinned() throws ParsingException {
        return getCommentRenderer().has("pinnedCommentBadge");
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return getCommentRenderer().has("authorCommentBadge");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        try {
            return getTextFromObject(JsonUtils.getObject(getCommentRenderer(), "authorText"));
        } catch (final Exception e) {
            return "";
        }
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        try {
            return "https://www.youtube.com/channel/" + JsonUtils.getString(getCommentRenderer(),
                    "authorEndpoint.browseEndpoint.browseId");
        } catch (final Exception e) {
            return "";
        }
    }

    @Override
    public int getReplyCount() throws ParsingException {
        final JsonObject commentRendererJsonObject = getCommentRenderer();
        if (commentRendererJsonObject.has("replyCount")) {
            return commentRendererJsonObject.getInt("replyCount");
        }
        return UNKNOWN_REPLY_COUNT;
    }

    @Override
    public Page getReplies() {
        try {
            final String id = JsonUtils.getString(
                    JsonUtils.getArray(json, "replies.commentRepliesRenderer.contents")
                            .getObject(0),
                    "continuationItemRenderer.continuationEndpoint.continuationCommand.token");
            return new Page(url, id);
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public boolean hasCreatorReply() throws ParsingException {
        try {
            final JsonObject commentRepliesRenderer = JsonUtils.getObject(json,
                    "replies.commentRepliesRenderer");
            return commentRepliesRenderer.has("viewRepliesCreatorThumbnail");
        } catch (final Exception e) {
            return false;
        }
    }

}
