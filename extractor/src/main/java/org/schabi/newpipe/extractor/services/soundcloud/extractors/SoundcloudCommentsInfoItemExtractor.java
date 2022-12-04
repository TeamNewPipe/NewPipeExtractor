package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

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

import java.util.Objects;

import javax.annotation.Nullable;

public class SoundcloudCommentsInfoItemExtractor implements CommentsInfoItemExtractor {
    public static final String BODY = "body";
    public static final String USER_PERMALINK = "permalink";
    public static final String USER_FULL_NAME = "full_name";
    public static final String USER_USERNAME = "username";

    private final JsonObject json;
    private final int index;
    private final JsonObject item;
    private final String url;
    private final JsonObject user;
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
        this.user = item.getObject("user");
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
        if (commentContent.startsWith("@")) {
            final String authorName = commentContent.split(" ", 2)[0].replace("@", "");
            final JsonArray comments = json.getArray(SoundcloudCommentsExtractor.COLLECTION);
            JsonObject author = null;
            for (int i = index - 1; i >= 0 && author == null; i--) {
                final JsonObject commentsAuthor = comments.getObject(i).getObject("user");
                // use startsWith because sometimes the mention of the user
                // is followed by a punctuation character.
                if (authorName.startsWith(commentsAuthor.getString(USER_PERMALINK))) {
                    author = commentsAuthor;
                }
            }
            if (author == null) {
                author = superComment.getObject("user");
            }
            final String name = isNullOrEmpty(author.getString(USER_FULL_NAME))
                    ? author.getString(USER_USERNAME) : author.getString(USER_FULL_NAME);
            final String link = "<a href=\"" + author.getString("permalink_url") + "\">"
                    + "@" + name + "</a>";
            commentContent = commentContent
                    .replace("@" + author.getString(USER_PERMALINK), link)
                    .replace("@" + author.getInt("user_id"), link);
        }

        return new Description(commentContent, Description.HTML);
    }

    @Override
    public String getUploaderName() {
        if (isNullOrEmpty(user.getString(USER_FULL_NAME))) {
            return user.getString(USER_USERNAME);
        }
        return user.getString(USER_FULL_NAME);
    }

    @Override
    public String getUploaderAvatarUrl() {
        return user.getString("avatar_url");
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return user.getBoolean("verified");
    }

    @Override
    public int getStreamPosition() throws ParsingException {
        return item.getInt("timestamp") / 1000; // convert milliseconds to seconds
    }

    @Override
    public String getUploaderUrl() {
        return user.getString("permalink_url");
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
        return user.getString(USER_PERMALINK);
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getThumbnailUrl() {
        return user.getString("avatar_url");
    }

    @Override
    public Page getReplies() {
        if (replyCount == CommentsInfoItem.UNKNOWN_REPLY_COUNT) {
            final JsonArray replies = new JsonArray();
            final CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(
                    ServiceList.SoundCloud.getServiceId());
            // SoundCloud has only comments and top level replies, but not nested replies.
            // Therefore, replies cannot have further replies.
            if (superComment == null) {
                // Loop through all comments which come after the original comment
                // to find its replies.
                final JsonArray allItems = json.getArray(SoundcloudCommentsExtractor.COLLECTION);
                boolean foundReply = false;
                for (int i = index + 1; i < allItems.size(); i++) {
                    final JsonObject comment = allItems.getObject(i);
                    if (SoundcloudParsingHelper.isReplyTo(item, comment)) {
                        replies.add(comment);
                        collector.commit(new SoundcloudCommentsInfoItemExtractor(
                                json, i, comment, url, item));
                        foundReply = true;
                    } else if (foundReply) {
                        // Only the comments directly after the original comment
                        // having the same timestamp are replies to the original comment.
                        // The first comment not having the same timestamp
                        // is the next top-level comment.
                        break;
                    }
                }
            }
            replyCount = replies.size();
            if (collector.getItems().isEmpty()) {
                return null;
            }
            repliesPage = new Page(getUrl(), getCommentId());
            repliesPage.setContent(json);
        }

        return repliesPage;
    }

    @Override
    public int getReplyCount() {
        if (replyCount == CommentsInfoItem.UNKNOWN_REPLY_COUNT) {
            getReplies();
        }
        return replyCount;
    }
}
