package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemsCollector;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;
import org.schabi.newpipe.extractor.utils.cache.SoundCloudCommentsCache;
import org.schabi.newpipe.extractor.utils.cache.SoundCloudCommentsCache.CachedCommentInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoundcloudCommentsExtractor extends CommentsExtractor {
    public static final String COLLECTION = "collection";
    public static final String NEXT_HREF = "next_href";

    /**
     * The last comment which was a top level comment.
     * Next pages might start with replies to the last top level comment
     * and therefore the {@link SoundcloudCommentsInfoItemExtractor#replyCount}
     * of the last top level comment cannot be determined certainly.
     */
    private static final SoundCloudCommentsCache LAST_TOP_LEVEL_COMMENTS =
            new SoundCloudCommentsCache(10);

    public SoundcloudCommentsExtractor(final StreamingService service,
                                       final ListLinkHandler uiHandler) {
        super(service, uiHandler);
    }

    @Nonnull
    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage() throws ExtractionException,
            IOException {
        final Downloader downloader = NewPipe.getDownloader();
        final Response response = downloader.get(getUrl());

        final JsonObject json;
        try {
            json = JsonParser.object().from(response.responseBody());
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse json", e);
        }

        final CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(
                getServiceId());

        collectCommentsFrom(collector, json, null);

        return new InfoItemsPage<>(collector, new Page(json.getString(NEXT_HREF)));
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getPage(final Page page)
            throws ExtractionException, IOException {

        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }
        final JsonObject json;
        final CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(
                getServiceId());

        // Replies typically do not have a next page, but that's not always the case.
        final boolean hasNextPage;
        if (page.hasContent()) {
            // This page contains the whole previously fetched comments.
            // We need to get the comments which are replies to the comment with the page's id.
            json = (JsonObject) page.getContent();
            try {
                final int commentId = Integer.parseInt(page.getId());
                hasNextPage = collectRepliesFrom(collector, json, commentId, page.getUrl());
            } catch (final NumberFormatException e) {
                throw new ParsingException("Got invalid comment id", e);
            }
        } else {

            final Downloader downloader = NewPipe.getDownloader();
            final Response response = downloader.get(page.getUrl());

            try {
                json = JsonParser.object().from(response.responseBody());
                hasNextPage = json.has(NEXT_HREF);
            } catch (final JsonParserException e) {
                throw new ParsingException("Could not parse json", e);
            }

            final CachedCommentInfo topLevelCommentElement = LAST_TOP_LEVEL_COMMENTS.get(getUrl());
            if (topLevelCommentElement == null) {
                if (LAST_TOP_LEVEL_COMMENTS.isEmpty()) {
                    collector.addError(new RuntimeException(
                            "Could not get last top level comment. It has been removed from cache."
                                    + " Increase the cache size to not loose any comments"));
                }
                collectCommentsFrom(collector, json, null);
            } else {
                collectCommentsFrom(collector, json, topLevelCommentElement);
            }
        }

        if (hasNextPage) {
            return new InfoItemsPage<>(collector, new Page(json.getString(NEXT_HREF)));
        } else {
            return new InfoItemsPage<>(collector, null);
        }
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) {
    }

    /**
     * Collect top level comments from a SoundCloud API response.
     *
     * @param collector           the collector which collects the the top level comments
     * @param json                the JsonObject of the API response
     * @param lastTopLevelComment the last top level comment from the previous page or {@code null}
     *                            if this method is run for the initial page.
     * @throws ParsingException
     */
    private void collectCommentsFrom(@Nonnull final CommentsInfoItemsCollector collector,
                                     @Nonnull final JsonObject json,
                                     @Nullable final CachedCommentInfo lastTopLevelComment)
            throws ParsingException {
        final List<SoundcloudCommentsInfoItemExtractor> extractors = new ArrayList<>();
        final String url = getUrl();

        JsonObject currentTopLevelComment = null;
        int currentTopLevelCommentIndex = 0;
        boolean isLastCommentReply = true;
        boolean isFirstCommentReply = false;
        boolean addedLastTopLevelComment = lastTopLevelComment == null;
        // Check whether the first comment in the list is a reply to the last top level comment
        // from the previous page if there was a previous page.
        if (lastTopLevelComment != null) {
            final JsonObject firstComment = json.getArray(COLLECTION).getObject(0);
            if (SoundcloudParsingHelper.isReplyTo(lastTopLevelComment.comment, firstComment)) {
                currentTopLevelComment = lastTopLevelComment.comment;
                isFirstCommentReply = true;
                merge(json, lastTopLevelComment.json, lastTopLevelComment.index);
            } else {
                extractors.add(new SoundcloudCommentsInfoItemExtractor(
                        lastTopLevelComment.json,
                        lastTopLevelComment.index,
                        lastTopLevelComment.comment, url, null));
                addedLastTopLevelComment = true;
            }
        }

        final JsonArray entries = json.getArray(COLLECTION);
        for (int i = 0; i < entries.size(); i++) {
            final JsonObject entry = entries.getObject(i);
            // Extract all top level comments
            // The first comment is a top level co
            // if it is not a reply to the last top level comment
            //
            if ((i == 0 && !isFirstCommentReply)
                    || (
                    i != 0 && !SoundcloudParsingHelper.isReplyTo(entries.getObject(i - 1), entry)
                            && !SoundcloudParsingHelper.isReplyTo(currentTopLevelComment, entry))) {
                currentTopLevelComment = entry;
                currentTopLevelCommentIndex = i;
                if (!addedLastTopLevelComment) {
                    // There is a new top level comment. This also means that we can now determine
                    // the reply count and get all replies for the top level comment.
                    extractors.add(new SoundcloudCommentsInfoItemExtractor(
                            json, 0, lastTopLevelComment.comment, url, null));
                    addedLastTopLevelComment = true;
                }
                if (i == entries.size() - 1) {
                    isLastCommentReply = false;
                    LAST_TOP_LEVEL_COMMENTS.put(getUrl(), currentTopLevelComment, json, i);

                    // Do not collect the last comment if it is a top level comment
                    // because it might have replies.
                    // That is information we cannot get from the comment itself
                    // (thanks SoundCloud...) but needs to be obtained from the next comment.
                    // The comment will therefore be collected
                    // when collecting the items from the next page.
                    break;
                }
                extractors.add(new SoundcloudCommentsInfoItemExtractor(
                        json, i, entry, url, null));
            }
        }
        if (isLastCommentReply) {
            // Do not collect the last top level comment if it has replies and the retrieved
            // comment list ends with a reply. We do not know whether the next page starts
            // with more replies to the last top level comment.
            LAST_TOP_LEVEL_COMMENTS.put(
                    getUrl(),
                    extractors.remove(extractors.size() - 1).item,
                    json, currentTopLevelCommentIndex);
        }
        extractors.stream().forEach(collector::commit);

    }

    /**
     * Collect replies to a top level comment from a SoundCloud API response.
     *
     * @param collector the collector which collects the the replies
     * @param json      the SoundCloud API response
     * @param id        the comment's id for which the replies are collected
     * @param url       the corresponding page's URL
     * @return {code true} if there might be more replies to the comment;
     * {@code false} if there are definitely no more replies
     */
    private boolean collectRepliesFrom(@Nonnull final CommentsInfoItemsCollector collector,
                                       @Nonnull final JsonObject json,
                                       final int id,
                                       @Nonnull final String url) {
        JsonObject originalComment = null;
        final JsonArray entries = json.getArray(COLLECTION);
        boolean moreReplies = false;
        for (int i = 0; i < entries.size(); i++) {
            final JsonObject comment = entries.getObject(i);
            if (comment.getInt("id") == id) {
                originalComment = comment;
                continue;
            }
            if (originalComment != null
                    && SoundcloudParsingHelper.isReplyTo(originalComment, comment)) {
                collector.commit(new SoundcloudCommentsInfoItemExtractor(
                        json, i, entries.getObject(i), url, originalComment));
                // There might be more replies to the originalComment
                // if the original comment is at the end of the list.
                if (i == entries.size() - 1 && json.has(NEXT_HREF)) {
                    moreReplies = true;
                }
            }
        }
        return moreReplies;
    }

    private void merge(@Nonnull final JsonObject target, @Nonnull final JsonObject subject,
                       final int index) {
        final JsonArray targetArray = target.getArray(COLLECTION);
        final JsonArray subjectArray = subject.getArray(COLLECTION);
        final JsonArray newArray = new JsonArray(
                targetArray.size() + subjectArray.size() - index - 1);
        for (int i = index; i < subjectArray.size(); i++) {
            newArray.add(subjectArray.getObject(i));
        }
        newArray.addAll(targetArray);
        target.put(COLLECTION, newArray);
    }

}
