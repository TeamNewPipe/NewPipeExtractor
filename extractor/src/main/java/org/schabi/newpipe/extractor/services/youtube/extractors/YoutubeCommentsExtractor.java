package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemsCollector;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class YoutubeCommentsExtractor extends CommentsExtractor {

    /**
     * Whether comments are disabled on video.
     */
    private boolean commentsDisabled;

    /**
     * The second ajax <b>/next</b> response.
     */
    private JsonObject ajaxJson;

    public YoutubeCommentsExtractor(
            final StreamingService service,
            final ListLinkHandler uiHandler) {
        super(service, uiHandler);
    }

    @Nonnull
    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage()
            throws IOException, ExtractionException {

        if (commentsDisabled) {
            return getInfoItemsPageForDisabledComments();
        }

        return extractComments(ajaxJson);
    }

    /**
     * Finds the initial comments token and initializes commentsDisabled.
     * <br/>
     * Also sets {@link #commentsDisabled}.
     *
     * @return the continuation token or null if none was found
     */
    @Nullable
    private String findInitialCommentsToken(final JsonObject nextResponse) {
        final JsonArray contents = getJsonContents(nextResponse);

        // For videos where comments are unavailable, this would be null
        if (contents == null) {
            return null;
        }

        final String token = contents
                .stream()
                // Only use JsonObjects
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                // Check if the comment-section is present
                .filter(jObj -> {
                    try {
                        return "comments-section".equals(
                                JsonUtils.getString(jObj, "itemSectionRenderer.targetId"));
                    } catch (final ParsingException ignored) {
                        return false;
                    }
                })
                .findFirst()
                // Extract the token (or null in case of error)
                .map(itemSectionRenderer -> {
                    try {
                        return JsonUtils.getString(
                                itemSectionRenderer
                                        .getObject("itemSectionRenderer")
                                        .getArray("contents").getObject(0),
                                "continuationItemRenderer.continuationEndpoint"
                                        + ".continuationCommand.token");
                    } catch (final ParsingException ignored) {
                        return null;
                    }
                })
                .orElse(null);

        // The comments are disabled if we couldn't get a token
        commentsDisabled = token == null;

        return token;
    }

    @Nullable
    private JsonArray getJsonContents(final JsonObject nextResponse) {
        try {
            return JsonUtils.getArray(nextResponse,
                    "contents.twoColumnWatchNextResults.results.results.contents");
        } catch (final ParsingException e) {
            return null;
        }
    }

    @Nonnull
    private InfoItemsPage<CommentsInfoItem> getInfoItemsPageForDisabledComments() {
        return new InfoItemsPage<>(Collections.emptyList(), null, Collections.emptyList());
    }

    @Nullable
    private Page getNextPage(@Nonnull final JsonObject jsonObject) throws ExtractionException {
        final JsonArray onResponseReceivedEndpoints =
                jsonObject.getArray("onResponseReceivedEndpoints");

        // Prevent ArrayIndexOutOfBoundsException
        if (onResponseReceivedEndpoints.isEmpty()) {
            return null;
        }

        final JsonArray continuationItemsArray;
        try {
            final JsonObject endpoint = onResponseReceivedEndpoints
                    .getObject(onResponseReceivedEndpoints.size() - 1);
            continuationItemsArray = endpoint
                    .getObject("reloadContinuationItemsCommand",
                            endpoint.getObject("appendContinuationItemsAction"))
                    .getArray("continuationItems");
        } catch (final Exception e) {
            return null;
        }
        // Prevent ArrayIndexOutOfBoundsException
        if (continuationItemsArray.isEmpty()) {
            return null;
        }

        final JsonObject continuationItemRenderer = continuationItemsArray
                .getObject(continuationItemsArray.size() - 1)
                .getObject("continuationItemRenderer");

        final String jsonPath = continuationItemRenderer.has("button")
                ? "button.buttonRenderer.command.continuationCommand.token"
                : "continuationEndpoint.continuationCommand.token";

        final String continuation;
        try {
            continuation = JsonUtils.getString(continuationItemRenderer, jsonPath);
        } catch (final Exception e) {
            return null;
        }
        return getNextPage(continuation);
    }

    @Nonnull
    private Page getNextPage(final String continuation) throws ParsingException {
        return new Page(getUrl(), continuation); // URL is ignored tho
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {

        if (commentsDisabled) {
            return getInfoItemsPageForDisabledComments();
        }

        if (page == null || isNullOrEmpty(page.getId())) {
            throw new IllegalArgumentException("Page doesn't have the continuation.");
        }

        final Localization localization = getExtractorLocalization();
        // @formatter:off
        final byte[] body = JsonWriter.string(
                prepareDesktopJsonBuilder(localization, getExtractorContentCountry())
                    .value("continuation", page.getId())
                    .done())
                .getBytes(StandardCharsets.UTF_8);
        // @formatter:on

        final JsonObject jsonObject = getJsonPostResponse("next", body, localization);

        return extractComments(jsonObject);
    }

    private InfoItemsPage<CommentsInfoItem> extractComments(final JsonObject jsonObject)
            throws ExtractionException {
        final CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(
                getServiceId());
        collectCommentsFrom(collector, jsonObject);
        return new InfoItemsPage<>(collector, getNextPage(jsonObject));
    }

    private void collectCommentsFrom(final CommentsInfoItemsCollector collector,
                                     final JsonObject jsonObject)
            throws ParsingException {

        final JsonArray onResponseReceivedEndpoints =
                jsonObject.getArray("onResponseReceivedEndpoints");
        // Prevent ArrayIndexOutOfBoundsException
        if (onResponseReceivedEndpoints.isEmpty()) {
            return;
        }
        final JsonObject commentsEndpoint =
                onResponseReceivedEndpoints.getObject(onResponseReceivedEndpoints.size() - 1);

        final String path;

        if (commentsEndpoint.has("reloadContinuationItemsCommand")) {
            path = "reloadContinuationItemsCommand.continuationItems";
        } else if (commentsEndpoint.has("appendContinuationItemsAction")) {
            path = "appendContinuationItemsAction.continuationItems";
        } else {
            // No comments
            return;
        }

        final JsonArray contents;
        try {
            contents = new JsonArray(JsonUtils.getArray(commentsEndpoint, path));
        } catch (final Exception e) {
            // No comments
            return;
        }

        final int index = contents.size() - 1;
        if (!contents.isEmpty() && contents.getObject(index).has("continuationItemRenderer")) {
            contents.remove(index);
        }

        final String jsonKey = contents.getObject(0).has("commentThreadRenderer")
                ? "commentThreadRenderer"
                : "commentRenderer";

        final List<Object> comments;
        try {
            comments = JsonUtils.getValues(contents, jsonKey);
        } catch (final Exception e) {
            throw new ParsingException("Unable to get parse youtube comments", e);
        }

        final String url = getUrl();
        comments.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(jObj -> new YoutubeCommentsInfoItemExtractor(jObj, url, getTimeAgoParser()))
                .forEach(collector::commit);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final Localization localization = getExtractorLocalization();
        // @formatter:off
        final byte[] body = JsonWriter.string(
                prepareDesktopJsonBuilder(localization, getExtractorContentCountry())
                    .value("videoId", getId())
                    .done())
                .getBytes(StandardCharsets.UTF_8);
        // @formatter:on

        final String initialToken =
                findInitialCommentsToken(getJsonPostResponse("next", body, localization));

        if (initialToken == null) {
            return;
        }

        // @formatter:off
        final byte[] ajaxBody = JsonWriter.string(
                        prepareDesktopJsonBuilder(localization, getExtractorContentCountry())
                                .value("continuation", initialToken)
                                .done())
                .getBytes(StandardCharsets.UTF_8);
        // @formatter:on

        ajaxJson = getJsonPostResponse("next", ajaxBody, localization);
    }


    @Override
    public boolean isCommentsDisabled() {
        return commentsDisabled;
    }

    @Override
    public int getCommentsCount() throws ExtractionException {
        assertPageFetched();

        if (commentsDisabled) {
            return -1;
        }

        final JsonObject countText = ajaxJson
                .getArray("onResponseReceivedEndpoints").getObject(0)
                .getObject("reloadContinuationItemsCommand")
                .getArray("continuationItems").getObject(0)
                .getObject("commentsHeaderRenderer")
                .getObject("countText");

        try {
            return Integer.parseInt(
                    Utils.removeNonDigitCharacters(getTextFromObject(countText))
            );
        } catch (final Exception e) {
            throw new ExtractionException("Unable to get comments count", e);
        }
    }
}
