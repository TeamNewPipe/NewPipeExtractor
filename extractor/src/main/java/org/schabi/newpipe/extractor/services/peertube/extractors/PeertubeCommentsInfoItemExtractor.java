package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import com.grack.nanojson.JsonWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeCommentsExtractor.CHILDREN;
import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject;
import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.parseDateFrom;

public class PeertubeCommentsInfoItemExtractor implements CommentsInfoItemExtractor {
    @Nonnull
    private final JsonObject item;
    @Nullable
    private final JsonArray children;
    @Nonnull
    private final String url;
    @Nonnull
    private final String baseUrl;
    private final boolean isReply;

    private Integer replyCount;

    public PeertubeCommentsInfoItemExtractor(@Nonnull final JsonObject item,
                                             @Nullable final JsonArray children,
                                             @Nonnull final String url,
                                             @Nonnull final String baseUrl,
                                             final boolean isReply) {
        this.item = item;
        this.children = children;
        this.url = url;
        this.baseUrl = baseUrl;
        this.isReply = isReply;
    }

    @Override
    public String getUrl() throws ParsingException {
        return url + "/" + getCommentId();
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() {
        return getUploaderAvatars();
    }

    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(item, "account.displayName");
    }

    @Override
    public String getTextualUploadDate() throws ParsingException {
        return JsonUtils.getString(item, "createdAt");
    }

    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        final String textualUploadDate = getTextualUploadDate();
        return new DateWrapper(parseDateFrom(textualUploadDate));
    }

    @Override
    public Description getCommentText() throws ParsingException {
        final String htmlText = JsonUtils.getString(item, "text");
        try {
            final Document doc = Jsoup.parse(htmlText);
            final var text = doc.body().text();
            return new Description(text, Description.PLAIN_TEXT);
        } catch (final Exception e) {
            final var text = htmlText.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");
            return new Description(text, Description.PLAIN_TEXT);
        }
    }

    @Override
    public String getCommentId() {
        return Objects.toString(item.getLong("id"), null);
    }

    @Nonnull
    @Override
    public List<Image> getUploaderAvatars() {
        return getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl, item.getObject("account"));
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return JsonUtils.getString(item, "account.name") + "@"
                + JsonUtils.getString(item, "account.host");
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        final String name = JsonUtils.getString(item, "account.name");
        final String host = JsonUtils.getString(item, "account.host");
        return ServiceList.PeerTube.getChannelLHFactory()
                .fromId("accounts/" + name + "@" + host, baseUrl).getUrl();
    }

    @Override
    @Nullable
    public Page getReplies() throws ParsingException {
        if (getReplyCount() == 0) {
            return null;
        }
        final String threadId = JsonUtils.getNumber(item, "threadId").toString();
        final String repliesUrl = url + "/" + threadId;
        if (isReply && children != null && !children.isEmpty()) {
            // Nested replies are already included in the original thread's request.
            // Wrap the replies into a JsonObject, because the original thread's request body
            // is also structured like a JsonObject.
            final JsonObject pageContent = new JsonObject();
            pageContent.put(CHILDREN, children);
            return new Page(repliesUrl, threadId,
                     JsonWriter.string(pageContent).getBytes(StandardCharsets.UTF_8));
        }
        return new Page(repliesUrl, threadId);
    }

    @Override
    public int getReplyCount() throws ParsingException {
        if (replyCount == null) {
            if (children != null && !children.isEmpty()) {
                // The totalReplies field is inaccurate for nested replies and sometimes returns 0
                // although there are replies to that reply stored in children.
                replyCount = children.size();
            } else {
                replyCount = JsonUtils.getNumber(item, "totalReplies").intValue();
            }
        }
        return replyCount;
    }

    @Override
    public boolean hasCreatorReply() {
        return item.has("totalRepliesFromVideoAuthor")
                && item.getInt("totalRepliesFromVideoAuthor") > 0;
    }
}
