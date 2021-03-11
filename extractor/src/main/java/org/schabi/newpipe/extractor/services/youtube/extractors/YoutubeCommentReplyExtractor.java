package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.comments.CommentReplyExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemsCollector;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class YoutubeCommentReplyExtractor extends CommentReplyExtractor {
    Map<String, List<String>> requestHeaders;
    private Page repliesPage;
    private Page nextPage;
    private CommentsInfoItemsCollector repliesCollector;
    private List<CommentsInfoItem> replies;
    private JsonObject ajaxJson;

    public YoutubeCommentReplyExtractor(final StreamingService service,
                                        final ListLinkHandler replyHandler) {
        super(service, replyHandler);
        this.repliesPage = new Page(replyHandler.getUrl());
        this.replies = Collections.emptyList();
    }

    @Override
    public void extractReplies() throws ParsingException {
        final JsonObject comment_replies = JsonUtils.getObject(this.ajaxJson,
                "response.continuationContents.commentRepliesContinuation");


        // Get the next page of replies, if it exists
        if (comment_replies.has("continuations")) {
            final JsonArray continuations
                    = JsonUtils.getArray(comment_replies, "continuations");

            final String continuationContents = continuations
                    .getObject(0)
                    .getString("nextContinuationData.continuation");
            if (Utils.isNullOrEmpty(continuationContents)) {
                this.nextPage = null;
            } else {
                final String contUrl = YoutubeParsingHelper.getRepliesUrl(continuationContents);
                this.nextPage = new Page(contUrl);
            }
        } else {
            this.nextPage = null;
        }

        // get the reply info, if it exists
        if (comment_replies.has("contents")) {
            final JsonArray contents = JsonUtils.getArray(comment_replies, "contents");
            List<Object> comments = JsonUtils.getValues(contents, "commentRenderer");
            final CommentsInfoItemsCollector collector
                    = new CommentsInfoItemsCollector(getServiceId());

            for (Object c : comments) {
                if (c instanceof JsonObject) {
                    YoutubeCommentsInfoItemExtractor extractor
                            = new YoutubeCommentsInfoItemExtractor((JsonObject) c,
                            this.repliesPage.getUrl(), getTimeAgoParser());
                    //The replies don't have replies
                    extractor.setReplyExtractor(null);
                    //But they are replies
                    extractor.setReplyState(true);
                    collector.commit(extractor);
                }
            }
            this.repliesCollector = collector;
            this.replies = collector.getCommentsInfoItemList();
        }
    }

    public void setRequestHeaders(Map<String, List<String>> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    @Override
    public Page getNextPage() {
        return this.nextPage;
    }

    @Nonnull
    @Override
    public InfoItemsPage getInitialPage() throws IOException, ExtractionException {
        return getInfoItemsPage();
    }

    @Override
    public InfoItemsPage getPage(Page page) throws IOException, ExtractionException {
        if (page == null) {
            return new ListExtractor.InfoItemsPage<CommentsInfoItem>(
                    new CommentsInfoItemsCollector(getServiceId()), null);
        }
        this.repliesPage = page;
        fetchPage();
        return new InfoItemsPage<CommentsInfoItem>(this.repliesCollector, this.nextPage);
    }

    @Override
    public List<CommentsInfoItem> getReplies() throws ParsingException {
        if (this.replies.isEmpty()) {
            extractReplies();
        }
        return this.replies;
    }

    public InfoItemsPage getInfoItemsPage() throws IOException, ExtractionException {
        return new InfoItemsPage<CommentsInfoItem>(this.repliesCollector, nextPage);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final Response response = downloader.get(this.repliesPage.getUrl(),
                requestHeaders, getExtractorLocalization());
        String responseBody = YoutubeParsingHelper.unescapeDocument(response.responseBody());
        try {
            this.ajaxJson = JsonParser.array().from(responseBody).getObject(1);
        } catch (JsonParserException e) {
            throw new ExtractionException(e.getMessage(), e);
        }
        extractReplies();
    }
}
