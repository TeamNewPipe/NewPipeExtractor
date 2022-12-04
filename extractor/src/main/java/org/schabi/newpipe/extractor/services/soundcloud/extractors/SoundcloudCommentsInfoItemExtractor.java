package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemsCollector;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;
import org.schabi.newpipe.extractor.stream.Description;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SoundcloudCommentsInfoItemExtractor implements CommentsInfoItemExtractor {
    public static final String USER = "user";
    public static final String BODY = "body";
    public static final String USER_PERMALINK = "permalink";

    private final JsonObject json;
    private final int index;
    private final JsonObject item;
    private final String url;
    private final JsonObject superComment;

    private int replyCount = CommentsInfoItem.UNKNOWN_REPLY_COUNT;
    private Page repliesPage = null;

    public SoundcloudCommentsInfoItemExtractor(final JsonObject json, final int index,
                                               final JsonObject item, final String url,
                                               @Nullable final JsonObject superComment) {
        this.json = json;
        this.index = index;
        this.item = item;
        this.url = url;
        this.superComment = superComment;
    }

    public SoundcloudCommentsInfoItemExtractor(final JsonObject json, final int index,
                                               final JsonObject item, final String url) {
        this(json, index, item, url, null);
    }

    @Override
    public String getCommentId() {
        return Objects.toString(item.getLong("id"), null);
    }

    @Override
    public Description getCommentText() {
        String commentContent = item.getString(BODY);
        if (superComment == null) {
            return new Description(commentContent, Description.PLAIN_TEXT);
        }
        // This comment is a reply to another comment.
        // Therefore, the comment starts with the mention of the original comment's author.
        // The account is automatically linked by the SoundCloud web UI.
        // We need to do this manually.
        final JsonObject user = superComment.getObject("user");
        final String link = "<a href=\"" + user.getString("permalink_url") + "\">"
                + "@" + user.getString("full_name") + "</a>";
        commentContent = commentContent
                .replace("@" + user.getString(USER_PERMALINK), link)
                .replace("@" + superComment.getInt("user_id"), link);

        return new Description(commentContent, Description.HTML);
    }

    @Override
    public String getUploaderName() {
        if (isNullOrEmpty(user.getString("full_name"))) {
            return user.getString("username");
        }
        return user.getString("full_name");
    }

    @Override
    public String getUploaderAvatarUrl() {
        return item.getObject(USER).getString("avatar_url");
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return item.getObject(USER).getBoolean("verified");
    }

    @Override
    public int getStreamPosition() throws ParsingException {
        return item.getInt("timestamp") / 1000; // convert milliseconds to seconds
    }

    @Override
    public String getUploaderUrl() {
        return item.getObject(USER).getString("permalink_url");
    }

    @Override
    public String getTextualUploadDate() {
        return item.getString("created_at");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return new DateWrapper(SoundcloudParsingHelper.parseDateFrom(getTextualUploadDate()));
    }

    @Override
    public String getName() throws ParsingException {
        return item.getObject(USER).getString("permalink");
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getThumbnailUrl() {
        return item.getObject(USER).getString("avatar_url");
    }

    @Override
    public Page getReplies() {
        if (replyCount == CommentsInfoItem.UNKNOWN_REPLY_COUNT) {
            final List<JsonObject> replies = new ArrayList<>();
            final CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(
                    ServiceList.SoundCloud.getServiceId());
            final JsonArray jsonArray = new JsonArray();
            // Replies start with the mention of the user who created the original comment.
            final String mention = "@" + item.getObject(USER).getString(USER_PERMALINK);
            // Loop through all comments which come after the original comment to find its replies.
            final JsonArray allItems = json.getArray(SoundcloudCommentsExtractor.COLLECTION);
            for (int i = index + 1; i < allItems.size(); i++) {
                final JsonObject comment = allItems.getObject(i);
                final String commentContent = comment.getString("body");
                if (commentContent.startsWith(mention)) {
                    replies.add(comment);
                    jsonArray.add(comment);
                    collector.commit(new SoundcloudCommentsInfoItemExtractor(
                            json, i, comment, url, item));
                } else if (!commentContent.startsWith("@") || replies.isEmpty()) {
                    // Only the comments directly after the original comment
                    // starting with the mention of the comment's creator
                    // are replies to the original comment.
                    // The first comment not starting with these letters
                    // is the next top-level comment.
                    break;
                }
            }
            replyCount = jsonArray.size();
            if (collector.getItems().isEmpty()) {
                return null;
            }
            repliesPage = new Page(getUrl(), getCommentId());
            repliesPage.setContent(json);
        }

        return repliesPage;
    }

    @Override
    public int getReplyCount() throws ParsingException {
        if (replyCount == CommentsInfoItem.UNKNOWN_REPLY_COUNT) {
            getReplies();
        }
        return replyCount;
    }
}
