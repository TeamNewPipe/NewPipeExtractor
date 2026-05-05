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
        final String text = extractChatMessageText(chatMessage.getObject("message"));
        return new Description(text, Description.PLAIN_TEXT);
    }

    /**
     * Extracts text from a live chat message, handling both regular text and emojis.
     * YouTube live chat messages use {@code runs} array where each element has either
     * {@code text} or {@code emoji}.
     */
    @Nonnull
    private static String extractChatMessageText(final JsonObject message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        if (message.has("simpleText")) {
            return message.getString("simpleText", "");
        }

        final JsonArray runs = message.getArray("runs");
        if (runs.isEmpty()) {
            return "";
        }

        final StringBuilder textBuilder = new StringBuilder();
        for (int i = 0; i < runs.size(); i++) {
            final JsonObject run = runs.getObject(i);
            if (run.has("text")) {
                final String text = run.getString("text", "");
                textBuilder.append(text);
            } else if (run.has("emoji")) {
                textBuilder.append(extractEmojiText(run.getObject("emoji")));
            }
        }

        return textBuilder.toString();
    }

    /**
     * Extracts a textual representation of a YouTube live chat emoji.
     * For standard emojis, {@code emojiId} contains the Unicode character.
     * For custom emojis, uses the first shortcut (e.g. {@code :wave:}) if available.
     */
    @Nonnull
    private static String extractEmojiText(final JsonObject emoji) {
        if (emoji == null || emoji.isEmpty()) {
            return "";
        }

        // For standard emojis, emojiId is the Unicode character itself.
        // For custom emojis, emojiId is an internal opaque ID that should not be displayed.
        // TODO: Consider rendering custom emoji thumbnails as <img> tags.
        if (!emoji.getBoolean("isCustomEmoji", false) && emoji.has("emojiId")) {
            final String emojiId = emoji.getString("emojiId", "");
            if (!emojiId.isEmpty()) {
                return emojiId;
            }
        }

        // Try to get shortcuts like ":wave:", ":heart:", ":face-blue-smiling:"
        if (emoji.has("shortcuts")) {
            final JsonArray shortcuts = emoji.getArray("shortcuts");
            for (int i = 0; i < shortcuts.size(); i++) {
                final String shortcut = shortcuts.getString(i, "");
                if (!shortcut.isEmpty()) {
                    return shortcut;
                }
            }
        }

        // Fallback: try searchTerms
        if (emoji.has("searchTerms")) {
            final JsonArray searchTerms = emoji.getArray("searchTerms");
            for (int i = 0; i < searchTerms.size(); i++) {
                final String term = searchTerms.getString(i, "");
                if (!term.isEmpty()) {
                    return ":" + term + ":";
                }
            }
        }

        return "[emoji]";
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
