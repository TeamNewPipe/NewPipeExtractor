package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.bulletComments.BulletCommentsExtractor;
import org.schabi.newpipe.extractor.bulletComments.BulletCommentsInfoItem;
import org.schabi.newpipe.extractor.bulletComments.BulletCommentsInfoItemsCollector;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.services.youtube.WatchDataCache;
import org.schabi.newpipe.extractor.services.youtube.YoutubeBulletCommentPair;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.ExtractorLogger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

public class YoutubeBulletCommentsExtractor extends BulletCommentsExtractor {
    private static final String TAG = "YTBCExtractor";
    private final boolean shoudldBeLive;
    private String lastContinuation;
    private ScheduledFuture<?> future;
    private boolean disabled = false;
    private long currentPlayPosition = 0;
    private long lastPlayPosition = 0;
    private final boolean isLiveStream;
    private final long startTime;
    private final String[] continuationKeyTexts = new String[]{
            "timedContinuationData", "invalidationContinuationData"
    };
    private final CopyOnWriteArrayList<YoutubeBulletCommentPair> messages =
            new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<YoutubeBulletCommentPair> superChatMessages =
            new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<String> idList = new CopyOnWriteArrayList<>();
    private boolean shouldSkipFetch = false;
    private ScheduledExecutorService executor;

    public YoutubeBulletCommentsExtractor(final StreamingService service,
                                          final ListLinkHandler uiHandler,
                                          final WatchDataCache watchDataCache)
            throws ExtractionException {
        super(service, uiHandler);
        ExtractorLogger.d(TAG, "Constructor called for url=" + uiHandler.getUrl()
                + " cacheCurrent=" + watchDataCache.currentUrl
                + " cacheLast=" + watchDataCache.lastCurrentUrl
                + " startAt=" + watchDataCache.startAt);
        if (watchDataCache.currentUrl.equals(uiHandler.getUrl())) {
            isLiveStream = watchDataCache.streamType.equals(StreamType.LIVE_STREAM);
            startTime = watchDataCache.startAt;
            shoudldBeLive = watchDataCache.shouldBeLive;
        } else if (watchDataCache.lastCurrentUrl.equals(uiHandler.getUrl())) {
            isLiveStream = watchDataCache.lastStreamType.equals(StreamType.LIVE_STREAM);
            startTime = watchDataCache.lastStartAt;
            shoudldBeLive = watchDataCache.lastShouldBeLive;
        } else {
            throw new ExtractionException(
                    "WatchDataCache of current url is not initialized");
        }
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        ExtractorLogger.d(TAG, "onFetchPage() called, url=" + getUrl()
                + " isLiveStream=" + isLiveStream + " shouldBeLive=" + shoudldBeLive);
        final String response = downloader.get(getUrl()).responseBody();
        if (response.contains("Live chat replay is not available")
                || response.contains("is disabled")
                || (!shoudldBeLive && !isLiveStream)) {
            ExtractorLogger.w(TAG, "Live chat disabled for this stream");
            disabled = true;
            return;
        }
        try {
            final String ytInitialData = response.split(
                    Pattern.quote("var ytInitialData = "))[1]
                    .split(Pattern.quote(";</script>"))[0];
            lastContinuation = JsonParser.object().from(ytInitialData)
                    .getObject("contents")
                    .getObject("twoColumnWatchNextResults")
                    .getObject("conversationBar")
                    .getObject("liveChatRenderer")
                    .getArray("continuations")
                    .getObject(0)
                    .getObject("reloadContinuationData")
                    .getString("continuation");
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchMessage() {
        ExtractorLogger.d(TAG, "fetchMessage() called, lastContinuation="
                + (lastContinuation != null ? "set" : "null")
                + " currentPlayPosition=" + currentPlayPosition);
        if (shouldSkipFetch) {
            shouldSkipFetch = false;
            return;
        }
        if (lastPlayPosition == currentPlayPosition) {
            return; // should only happen when watching replay and user pauses
            // we do not want to fetch the same message twice
        }
        if (lastContinuation == null) {
            return;
        }
        try {
            final byte[] json = JsonWriter.string(prepareDesktopJsonBuilder(
                            Localization.DEFAULT, ContentCountry.DEFAULT)
                            .value("continuation", lastContinuation)
                            .object("currentPlayerState")
                            .value("playerOffsetMs", String.valueOf(currentPlayPosition))
                            .end()
                            .done())
                    .getBytes(UTF_8);
            final JsonObject result;
            try {
                result = getJsonPostResponse("live_chat/"
                                + (isLiveStream ? "get_live_chat" : "get_live_chat_replay"),
                        json, Localization.DEFAULT);
            } catch (final Exception e) {
                return;
            }

            final JsonObject liveChatContinuation = result.getObject("continuationContents")
                    .getObject("liveChatContinuation");
            final JsonArray temp1 = liveChatContinuation.getArray("continuations");
            final JsonObject lastContinuationParent = temp1.getObject(
                    (!isLiveStream && temp1.size() == 2) ? 1 : 0);
            if (isLiveStream) {
                for (final String i : continuationKeyTexts) {
                    if (lastContinuationParent.has(i)) {
                        lastContinuation = lastContinuationParent.getObject(i)
                                .getString("continuation");
                        break;
                    }
                    if (i.equals(continuationKeyTexts[1])) {
                        throw new ParsingException(
                                "Failed to get continuation data");
                    }
                }
            } else {
                lastContinuation = lastContinuationParent
                        .getObject("playerSeekContinuationData")
                        .getString("continuation");
                if (lastContinuation == null) {
                    throw new ParsingException("Failed to get continuation data");
                }
            }

            lastPlayPosition = currentPlayPosition;

            final JsonArray actions = liveChatContinuation.getArray("actions");
            ExtractorLogger.d(TAG, "fetchMessage() got " + actions.size() + " actions");
            for (int i = 0; i < actions.size(); i++) {
                final JsonObject item = isLiveStream
                        ? actions.getObject(i).getObject("addChatItemAction")
                                .getObject("item")
                        : actions.getObject(i).getObject("replayChatItemAction")
                                .getArray("actions").getObject(0)
                                .getObject("addChatItemAction")
                                .getObject("item");
                if (item.has("liveChatTextMessageRenderer")) {
                    final JsonObject temp = item.getObject("liveChatTextMessageRenderer");
                    final String id = temp.getString("id");
                    if (!idList.contains(id)) {
                        messages.add(new YoutubeBulletCommentPair(temp, isLiveStream
                                ? -1 : Long.parseLong(actions.getObject(i)
                                .getObject("replayChatItemAction")
                                .getString("videoOffsetTimeMsec"))));
                        idList.add(id);
                    }
                } else if (item.has("liveChatPaidMessageRenderer")) {
                    final JsonObject temp = item.getObject("liveChatPaidMessageRenderer");
                    final String id = temp.getString("id");
                    if (!idList.contains(id)) {
                        superChatMessages.add(new YoutubeBulletCommentPair(temp, isLiveStream
                                ? -1 : Long.parseLong(actions.getObject(i)
                                .getObject("replayChatItemAction")
                                .getString("videoOffsetTimeMsec"))));
                        idList.add(id);
                    }
                }
            }
        } catch (final Exception e) {
            // should never throw any exception as that will stop fetching
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage<BulletCommentsInfoItem> getInitialPage()
            throws IOException, ExtractionException {
        ExtractorLogger.d(TAG, "getInitialPage() called, disabled=" + isDisabled());
        if (isDisabled()) {
            return null;
        }
        executor = Executors.newSingleThreadScheduledExecutor();
        future = executor.scheduleAtFixedRate(this::fetchMessage,
                1000, 1000, TimeUnit.MILLISECONDS);
        ExtractorLogger.d(TAG, "Live chat polling started");
        return null;
    }

    @Override
    public InfoItemsPage<BulletCommentsInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        return null;
    }

    @Override
    public boolean isLive() {
        return true;
    }

    @Override
    public List<BulletCommentsInfoItem> getLiveMessages() throws ParsingException {
        ExtractorLogger.d(TAG, "getLiveMessages() called, messages=" + messages.size()
                + " superChats=" + superChatMessages.size());
        final BulletCommentsInfoItemsCollector collector =
                new BulletCommentsInfoItemsCollector(getServiceId());
        for (final YoutubeBulletCommentPair item : messages) {
            collector.commit(new YoutubeBulletCommentsInfoItemExtractor(
                    item.getData(), startTime, item.getOffsetDuration()));
        }
        for (final YoutubeBulletCommentPair item : superChatMessages) {
            collector.commit(new YoutubeSuperChatInfoItemExtractor(
                    item.getData(), startTime, item.getOffsetDuration()));
        }
        messages.clear();
        superChatMessages.clear();
        return collector.getItems();
    }

    @Override
    public void disconnect() {
        ExtractorLogger.d(TAG, "disconnect() called");
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
        }
    }

    @Override
    public void reconnect() {
        ExtractorLogger.d(TAG, "reconnect() called, disabled=" + isDisabled());
        if (!isDisabled() && future != null && future.isCancelled()) {
            future = executor.scheduleAtFixedRate(this::fetchMessage,
                    1000, 1000, TimeUnit.MILLISECONDS);
            ExtractorLogger.d(TAG, "Live chat polling restarted");
        }
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void setCurrentPlayPosition(final long currentPlayPosition) {
        ExtractorLogger.d(TAG, "setCurrentPlayPosition() called, position="
                + currentPlayPosition);
        // 49 is -1 + 50, invalid and shouldn't set position
        // or it will causing duplicate messages
        if (!this.isLiveStream && currentPlayPosition == 49) {
            return;
        }
        if (this.currentPlayPosition > currentPlayPosition) {
            idList.clear();
            shouldSkipFetch = true;
        }
        this.currentPlayPosition = currentPlayPosition;
    }

    @Override
    public void clearMappingState() {
        idList.clear();
    }
}
