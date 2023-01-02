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
    @Nullable private JsonObject lastTopLevelComment;

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
            collectCommentsFrom(collector, json, lastTopLevelComment);
        }

        if (hasNextPage) {
            return new InfoItemsPage<>(collector, new Page(json.getString(NEXT_HREF)));
        } else {
            return new InfoItemsPage<>(collector, null);
        }
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) { }

    /**
     * Collect top level comments from a SoundCloud API response.
     * @param collector the collector which collects the the top level comments
     * @param json the JsonObject of the API response
     * @param lastTopLevelComment the last top level comment from the previous page or {@code null}
     *                            if this method is run for the initial page.
     * @throws ParsingException
     */
    private void collectCommentsFrom(@Nonnull final CommentsInfoItemsCollector collector,
                                     @Nonnull final JsonObject json,
                                     @Nullable final JsonObject lastTopLevelComment)
            throws ParsingException {
        final List<SoundcloudCommentsInfoItemExtractor> extractors = new ArrayList<>();
        final String url = getUrl();
        final JsonArray entries = json.getArray(COLLECTION);
        /**
         * The current top level comment.
         */
        JsonObject currentTopLevelComment = null;
        boolean isLastCommentReply = true;
        // Check whether the first comment in the list is a reply to the last top level comment
        // from the previous page if there was a previous page.
        if (lastTopLevelComment != null) {
            final JsonObject firstComment = entries.getObject(0);
            if (SoundcloudParsingHelper.isReplyTo(lastTopLevelComment, firstComment)) {
                currentTopLevelComment = lastTopLevelComment;
            } else {
                extractors.add(new SoundcloudCommentsInfoItemExtractor(
                        json, SoundcloudCommentsInfoItemExtractor.PREVIOUS_PAGE_INDEX,
                        firstComment, url, null));
            }
        }

        for (int i = 0; i < entries.size(); i++) {
            final JsonObject entry = entries.getObject(i);
            // extract all top level comments
            // The first comment is either a top level comment
            // if it is not a reply to the last top level comment
            //
            if (i == 0 && currentTopLevelComment == null
                    || (!SoundcloudParsingHelper.isReplyTo(entries.getObject(i - 1), entry)
                    && !SoundcloudParsingHelper.isReplyTo(currentTopLevelComment, entry))) {
                currentTopLevelComment = entry;
                if (i == entries.size() - 1) {
                    isLastCommentReply = false;
                    this.lastTopLevelComment = currentTopLevelComment;
                    // Do not collect the last comment if it is a top level comment
                    // because it might have replies.
                    // That is information we cannot get from the comment itself
                    // (thanks SoundCloud...) but needs to be obtained from the next comment.
                    // The comment will therefore be collected
                    // when collecting the items from the next page.
                    break;
                }
                extractors.add(new SoundcloudCommentsInfoItemExtractor(
                        json, i, entry, url, lastTopLevelComment));
            }
        }
        if (isLastCommentReply) {
            // Do not collect the last top level comment if it has replies and the retrieved
            // comment list ends with a reply. We do not know whether the next page starts
            // with more replies to the last top level comment.
            this.lastTopLevelComment = extractors.remove(extractors.size() - 1).item;
        }
        extractors.stream().forEach(collector::commit);

    }

    /**
     * Collect replies to a top level comment from a SoundCloud API response.
     * @param collector the collector which collects the the replies
     * @param json the SoundCloud API response
     * @param id the comment's id for which the replies are collected
     * @param url the corresponding page's URL
     * @return
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
                // There might be more replies to the originalComment,
                // especially if the original comment is at the end of the list.
                if (i == entries.size() - 1 && json.has(NEXT_HREF)) {
                    moreReplies = true;
                }
            }
        }
        return moreReplies;
    }

}
