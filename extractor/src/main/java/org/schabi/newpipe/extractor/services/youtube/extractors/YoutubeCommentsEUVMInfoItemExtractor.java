package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeDescriptionHelper.attributedDescriptionToHtml;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/**
 * A {@link CommentsInfoItemExtractor} for YouTube comment data returned in a view model and entity
 * updates.
 */
class YoutubeCommentsEUVMInfoItemExtractor implements CommentsInfoItemExtractor {

    private static final String AUTHOR = "author";
    private static final String PROPERTIES = "properties";

    @Nonnull
    private final JsonObject commentViewModel;
    @Nullable
    private final JsonObject commentRepliesRenderer;
    @Nonnull
    private final JsonObject commentEntityPayload;
    @Nonnull
    private final JsonObject engagementToolbarStateEntityPayload;
    @Nonnull
    private final String videoUrl;
    @Nonnull
    private final TimeAgoParser timeAgoParser;

    YoutubeCommentsEUVMInfoItemExtractor(
            @Nonnull final JsonObject commentViewModel,
            @Nullable final JsonObject commentRepliesRenderer,
            @Nonnull final JsonObject commentEntityPayload,
            @Nonnull final JsonObject engagementToolbarStateEntityPayload,
            @Nonnull final String videoUrl,
            @Nonnull final TimeAgoParser timeAgoParser) {
        this.commentViewModel = commentViewModel;
        this.commentRepliesRenderer = commentRepliesRenderer;
        this.commentEntityPayload = commentEntityPayload;
        this.engagementToolbarStateEntityPayload = engagementToolbarStateEntityPayload;
        this.videoUrl = videoUrl;
        this.timeAgoParser = timeAgoParser;
    }

    @Override
    public String getName() throws ParsingException {
        return getUploaderName();
    }

    @Override
    public String getUrl() throws ParsingException {
        return videoUrl;
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        return getUploaderAvatars();
    }

    @Override
    public int getLikeCount() throws ParsingException {
        final String textualLikeCount = getTextualLikeCount();
        try {
            if (Utils.isBlank(textualLikeCount)) {
                return 0;
            }

            return (int) Utils.mixedNumberWordToLong(textualLikeCount);
        } catch (final Exception e) {
            throw new ParsingException(
                    "Unexpected error while converting textual like count to like count", e);
        }
    }

    @Override
    public String getTextualLikeCount() {
        return commentEntityPayload.getObject("toolbar")
                .getString("likeCountNotliked");
    }

    @Override
    public Description getCommentText() throws ParsingException {
        // Comments' text work in the same way as an attributed video description
        return new Description(
                attributedDescriptionToHtml(commentEntityPayload.getObject(PROPERTIES)
                        .getObject("content")), Description.HTML);
    }

    @Override
    public String getTextualUploadDate() throws ParsingException {
        return commentEntityPayload.getObject(PROPERTIES)
                .getString("publishedTime");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        final String textualPublishedTime = getTextualUploadDate();
        if (isNullOrEmpty(textualPublishedTime)) {
            return null;
        }

        return timeAgoParser.parse(textualPublishedTime);
    }

    @Override
    public String getCommentId() throws ParsingException {
        String commentId = commentEntityPayload.getObject(PROPERTIES)
                .getString("commentId");
        if (isNullOrEmpty(commentId)) {
            commentId = commentViewModel.getString("commentId");
            if (isNullOrEmpty(commentId)) {
                throw new ParsingException("Could not get comment ID");
            }
        }
        return commentId;
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        final JsonObject author = commentEntityPayload.getObject(AUTHOR);
        String channelId = author.getString("channelId");
        if (isNullOrEmpty(channelId)) {
            channelId = author.getObject("channelCommand")
                    .getObject("innertubeCommand")
                    .getObject("browseEndpoint")
                    .getString("browseId");
            if (isNullOrEmpty(channelId)) {
                channelId = author.getObject("avatar")
                        .getObject("endpoint")
                        .getObject("innertubeCommand")
                        .getObject("browseEndpoint")
                        .getString("browseId");
                if (isNullOrEmpty(channelId)) {
                    throw new ParsingException("Could not get channel ID");
                }
            }
        }
        return "https://www.youtube.com/channel/" + channelId;
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return commentEntityPayload.getObject(AUTHOR)
                .getString("displayName");
    }

    @Nonnull
    @Override
    public List<Image> getUploaderAvatars() throws ParsingException {
        return getImagesFromThumbnailsArray(commentEntityPayload.getObject("avatar")
                .getObject("image")
                .getArray("sources"));
    }

    @Override
    public boolean isHeartedByUploader() {
        return "TOOLBAR_HEART_STATE_HEARTED".equals(
                engagementToolbarStateEntityPayload.getString("heartState"));
    }

    @Override
    public boolean isPinned() {
        return commentViewModel.has("pinnedText");
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        final JsonObject author = commentEntityPayload.getObject(AUTHOR);
        return author.getBoolean("isVerified") || author.getBoolean("isArtist");
    }

    @Override
    public int getReplyCount() throws ParsingException {
        // As YouTube allows replies up to 750 comments, we cannot check if the count returned is a
        // mixed number or a real number
        // Assume it is a mixed one, as it matches how numbers of most properties are returned
        final String replyCountString = commentEntityPayload.getObject("toolbar")
                .getString("replyCount");
        if (isNullOrEmpty(replyCountString)) {
            return 0;
        }
        return (int) Utils.mixedNumberWordToLong(replyCountString);
    }

    @Nullable
    @Override
    public Page getReplies() throws ParsingException {
        if (isNullOrEmpty(commentRepliesRenderer)) {
            return null;
        }

        final String continuation = commentRepliesRenderer.getArray("contents")
                .stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(content -> content.getObject("continuationItemRenderer", null))
                .filter(Objects::nonNull)
                .findFirst()
                .map(continuationItemRenderer ->
                                continuationItemRenderer.getObject("continuationEndpoint")
                                        .getObject("continuationCommand")
                                        .getString("token"))
                .orElseThrow(() ->
                        new ParsingException("Could not get comment replies continuation"));
        return new Page(videoUrl, continuation);
    }

    @Override
    public boolean isChannelOwner() {
        return commentEntityPayload.getObject(AUTHOR)
                .getBoolean("isCreator");
    }

    @Override
    public boolean hasCreatorReply() {
        return commentRepliesRenderer != null
                && commentRepliesRenderer.has("viewRepliesCreatorThumbnail");
    }
}
