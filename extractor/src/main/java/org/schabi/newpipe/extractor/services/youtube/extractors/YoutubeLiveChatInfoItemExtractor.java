package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.Description;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Extracts comment info from a YouTube live chat message.
 */
public class YoutubeLiveChatInfoItemExtractor implements CommentsInfoItemExtractor {

    private final JsonObject chatMessage;

    public YoutubeLiveChatInfoItemExtractor(final JsonObject chatMessage) {
        this.chatMessage = chatMessage;
    }

    @Nonnull
    @Override
    public Description getCommentText() throws ParsingException {
        return new Description(YoutubeParsingHelper.getTextFromObject(
                chatMessage.getObject("message")), Description.PLAIN_TEXT);
    }

    @Override
    public String getCommentId() throws ParsingException {
        return chatMessage.getString("id", "");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return YoutubeParsingHelper.getTextFromObject(
                chatMessage.getObject("authorName"));
    }

    @Nonnull
    @Override
    public List<Image> getUploaderAvatars() throws ParsingException {
        try {
            final JsonArray thumbnails = chatMessage.getObject("authorPhoto")
                    .getArray("thumbnails");
            return YoutubeParsingHelper.getImagesFromThumbnailsArray(thumbnails);
        } catch (final Exception e) {
            return List.of();
        }
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        try {
            return YoutubeParsingHelper.getUrlFromNavigationEndpoint(
                    chatMessage.getObject("authorEndpoint"));
        } catch (final Exception e) {
            return "";
        }
    }

    @Override
    public boolean isChannelOwner() throws ParsingException {
        final JsonArray badges = chatMessage.getArray("authorBadges");
        for (int i = 0; i < badges.size(); i++) {
            final JsonObject badge = badges.getObject(i);
            if (badge.has("liveChatAuthorBadgeRenderer")) {
                final JsonObject renderer = badge.getObject(
                        "liveChatAuthorBadgeRenderer");
                if (renderer.has("icon")) {
                    final String iconType = renderer.getObject("icon")
                            .getString("iconType", "");
                    if ("owner".equals(iconType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        return Collections.emptyList();
    }

    @Override
    public String getName() throws ParsingException {
        return getUploaderName();
    }

    @Override
    public String getUrl() throws ParsingException {
        return "";
    }
}
