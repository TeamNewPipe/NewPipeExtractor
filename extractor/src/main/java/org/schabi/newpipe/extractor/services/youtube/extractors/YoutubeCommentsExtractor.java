package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;

public class YoutubeCommentsExtractor extends CommentsExtractor {

    private JsonObject nextResponse;

    /**
     * Caching mechanism and holder of the commentsDisabled value.
     * <br/>
     * Initial value = empty -> unknown if comments are disabled or not<br/>
     * Some method calls {@link #findInitialCommentsToken()}
     * -> value is set<br/>
     * If the method or another one that is depending on disabled comments
     * is now called again, the method execution can avoid unnecessary calls
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Boolean> optCommentsDisabled = Optional.empty();

    public YoutubeCommentsExtractor(
            final StreamingService service,
            final ListLinkHandler uiHandler) {
        super(service, uiHandler);
    }

    @Nonnull
    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage()
            throws IOException, ExtractionException {

        // Check if findInitialCommentsToken was already called and optCommentsDisabled initialized
        if (optCommentsDisabled.orElse(false)) {
            return getInfoItemsPageForDisabledComments();
        }

        // Get the token
        final String commentsToken = findInitialCommentsToken();
        // Check if the comments have been disabled
        if (optCommentsDisabled.get()) {
            return getInfoItemsPageForDisabledComments();
        }

        return getPage(getNextPage(commentsToken));
    }

    /**
     * Finds the initial comments token and initializes commentsDisabled.
     * <br/>
     * Also sets {@link #optCommentsDisabled}.
     *
     * @return the continuation token or null if none was found
     */
    @Nullable
    private String findInitialCommentsToken() throws ExtractionException {
        final String token = JsonUtils.getArray(nextResponse,
                "contents.twoColumnWatchNextResults.results.results.contents")
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
        optCommentsDisabled = Optional.of(token == null);

        return token;
    }

    @Nonnull
    private InfoItemsPage<CommentsInfoItem> getInfoItemsPageForDisabledComments() {
        return new InfoItemsPage<>(Collections.emptyList(), null, Collections.emptyList());
    }

    @Nullable
    private Page getNextPage(@Nonnull final JsonObject ajaxJson) throws ExtractionException {
        final JsonArray onResponseReceivedEndpoints =
                ajaxJson.getArray("onResponseReceivedEndpoints");

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
        if (optCommentsDisabled.orElse(false)) {
            return getInfoItemsPageForDisabledComments();
        }
        if (page == null || isNullOrEmpty(page.getId())) {
            throw new IllegalArgumentException("Page doesn't have the continuation.");
        }

        final Localization localization = getExtractorLocalization();
        final byte[] body = JsonWriter.string(
                prepareDesktopJsonBuilder(localization, getExtractorContentCountry())
                    .value("continuation", page.getId())
                    .done())
                .getBytes(StandardCharsets.UTF_8);

        final JsonObject ajaxJson = getJsonPostResponse("next", body, localization);

        final CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(
                getServiceId());
        collectCommentsFrom(collector, ajaxJson);
        return new InfoItemsPage<>(collector, getNextPage(ajaxJson));
    }

    private void collectCommentsFrom(final CommentsInfoItemsCollector collector,
                                     @Nonnull final JsonObject ajaxJson) throws ParsingException {

        final JsonArray onResponseReceivedEndpoints =
                ajaxJson.getArray("onResponseReceivedEndpoints");
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
        final byte[] body = JsonWriter.string(
                prepareDesktopJsonBuilder(localization, getExtractorContentCountry())
                    .value("videoId", getId())
                    .done())
                .getBytes(StandardCharsets.UTF_8);

        nextResponse = getJsonPostResponse("next", body, localization);
    }


    @Override
    public boolean isCommentsDisabled() throws ExtractionException {
        // Check if commentsDisabled has to be initialized
        if (!optCommentsDisabled.isPresent()) {
            // Initialize commentsDisabled
            this.findInitialCommentsToken();
        }

        return optCommentsDisabled.get();
    }
}
