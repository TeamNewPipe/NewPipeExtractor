package org.schabi.newpipe.extractor.services.youtube.extractors.kiosk;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getClientVersion;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareJsonBuilder;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.InnertubeClientRequestInfo;
import org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamInfoItemExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamInfoItemLockupExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

abstract class YoutubeDesktopBaseKioskExtractor extends KioskExtractor<StreamInfoItem> {

    protected final String browseId;
    protected final String params;

    protected YoutubeChannelHelper.ChannelResponseData responseData;

    protected YoutubeDesktopBaseKioskExtractor(final StreamingService streamingService,
                                               final ListLinkHandler linkHandler,
                                               final String kioskId,
                                               final String browseId,
                                               final String params) {
        super(streamingService, linkHandler, kioskId);
        this.browseId = browseId;
        this.params = params;
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        responseData = YoutubeChannelHelper.getChannelResponse(
                browseId,
                params,
                getExtractorLocalization(),
                getExtractorContentCountry());
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return YoutubeChannelHelper.getChannelName(
                YoutubeChannelHelper.getChannelHeader(responseData.jsonResponse),
                null,
                responseData.jsonResponse);
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        final JsonObject tabRendererContent = responseData.jsonResponse.getObject("contents")
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs")
                .getObject(0)
                .getObject("tabRenderer")
                .getObject("content");

        final JsonArray tabContents;
        if (tabRendererContent.has("sectionListRenderer")) {
            tabContents = tabRendererContent.getObject("sectionListRenderer")
                    .getArray("contents")
                    .getObject(0)
                    .getObject("itemSectionRenderer")
                    .getArray("contents")
                    .getObject(0)
                    .getObject("shelfRenderer")
                    .getObject("content")
                    .getObject("gridRenderer")
                    .getArray("items");
        } else if (tabRendererContent.has("richGridRenderer")) {
            tabContents = tabRendererContent.getObject("richGridRenderer")
                    .getArray("contents");
        } else {
            tabContents = new JsonArray();
        }

        return collectStreamItems(tabContents,
                responseData.jsonResponse.getObject("responseContext")
                        .getString("visitorData"));
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        if (page == null || page.getBody() == null) {
            throw new IllegalArgumentException("Page is null or doesn't contain a body");
        }

        final JsonObject continuationResponse = getJsonPostResponse("browse", page.getBody(),
                getExtractorLocalization());

        final JsonArray continuationItems =
                continuationResponse.getArray("onResponseReceivedActions")
                        .stream()
                        .filter(JsonObject.class::isInstance)
                        .map(JsonObject.class::cast)
                        .filter(jsonObject -> jsonObject.has("appendContinuationItemsAction"))
                        .map(jsonObject -> jsonObject.getObject("appendContinuationItemsAction"))
                        .findFirst()
                        .orElse(new JsonObject())
                        .getArray("continuationItems");

        // The page ID is the visitor data
        return collectStreamItems(continuationItems, page.getId());
    }

    private InfoItemsPage<StreamInfoItem> collectStreamItems(
            @Nonnull final JsonArray items,
            @Nullable final String visitorData) throws IOException, ExtractionException {
        final var collector = new StreamInfoItemsCollector(getServiceId());

        final Page nextPage;
        if (items.isEmpty()) {
            nextPage = null;
        } else {
            final var timeAgoParser = getTimeAgoParser();
            items.streamAsJsonObjects()
                    .forEachOrdered(content -> {
                        if (content.has("richItemRenderer")) {
                            final JsonObject richItem = content.getObject("richItemRenderer")
                                    .getObject("content");

                            if (richItem.has("videoRenderer")) {
                                collector.commit(new YoutubeStreamInfoItemExtractor(
                                        richItem.getObject("videoRenderer"), timeAgoParser));
                            }
                        } else if (content.has("gridVideoRenderer")) {
                            collector.commit(new YoutubeStreamInfoItemExtractor(
                                    content.getObject("gridVideoRenderer"), timeAgoParser));
                        } else if (content.has("lockupViewModel")) {
                            // lockupViewModels are not used yet, but may be in the future
                            final JsonObject lockupViewModel = content.getObject("lockupViewModel");
                            if ("LOCKUP_CONTENT_TYPE_VIDEO".equals(
                                    lockupViewModel.getString("contentType"))) {
                                collector.commit(new YoutubeStreamInfoItemLockupExtractor(
                                        lockupViewModel, timeAgoParser));
                            }
                        }
                    });

            final JsonObject lastContent = items.getObject(items.size() - 1);
            if (lastContent.has("continuationItemRenderer")) {
                nextPage = getNextPageFrom(
                        lastContent.getObject("continuationItemRenderer"), visitorData);
            } else {
                nextPage = null;
            }
        }

        return new InfoItemsPage<>(collector, nextPage);
    }


    @Nullable
    private Page getNextPageFrom(@Nullable final JsonObject continuation,
                                 @Nullable final String visitorData)
            throws IOException, ExtractionException {
        if (isNullOrEmpty(continuation)) {
            return null;
        }

        final JsonObject continuationEndpoint = continuation.getObject("continuationEndpoint");
        final String continuationToken = continuationEndpoint.getObject("continuationCommand")
                .getString("token");

        // Visitor data is required to get videos in continuations, so we need to apply it to the
        // next page and save it as an ID so it can be applied to future continuations
        final InnertubeClientRequestInfo webClientRequestInfo =
                InnertubeClientRequestInfo.ofWebClient();
        webClientRequestInfo.clientInfo.clientVersion = getClientVersion();
        webClientRequestInfo.clientInfo.visitorData = visitorData;

        final byte[] body = JsonWriter.string(prepareJsonBuilder(getExtractorLocalization(),
                        getExtractorContentCountry(),
                        webClientRequestInfo,
                        null)
                        .value("continuation", continuationToken)
                        .done())
                .getBytes(StandardCharsets.UTF_8);

        // The URL is not needed and used, it is only provided to make Page.isValid return true
        return new Page(YOUTUBEI_V1_URL + "browse?" + DISABLE_PRETTY_PRINT_PARAMETER, visitorData,
                null, null, body);
    }
}
