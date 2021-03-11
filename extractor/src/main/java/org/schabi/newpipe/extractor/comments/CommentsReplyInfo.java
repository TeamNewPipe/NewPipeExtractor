package org.schabi.newpipe.extractor.comments;

import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.ExtractorHelper;

import java.io.IOException;

public class CommentsReplyInfo extends CommentsInfo {

    CommentReplyExtractor commentsExtractor;

    private CommentsReplyInfo(CommentReplyExtractor commentReplyExtractor)
            throws ParsingException {
        super(commentReplyExtractor.getServiceId(),
                commentReplyExtractor.getLinkHandler(), commentReplyExtractor.getName());
    }

    public static CommentsReplyInfo getInfo(CommentReplyExtractor commentReplyExtractor)
            throws IOException, ExtractionException {
        commentReplyExtractor.fetchPage();
        CommentsReplyInfo commentsReplyInfo = new CommentsReplyInfo(commentReplyExtractor);
        commentsReplyInfo.setCommentsExtractor(commentReplyExtractor);
        ListExtractor.InfoItemsPage<CommentsInfoItem> initialCommentsPage
                = ExtractorHelper.getItemsPageOrLogError(commentsReplyInfo, commentReplyExtractor);
        commentsReplyInfo.setRelatedItems(initialCommentsPage.getItems());
        commentsReplyInfo.setNextPage(initialCommentsPage.getNextPage());

        return commentsReplyInfo;
    }

    public static ListExtractor.InfoItemsPage<CommentsInfoItem>
    getMoreItems(CommentReplyExtractor commentReplyExtractor)
            throws IOException, ExtractionException {
        Page nextRepliesPage = commentReplyExtractor.getNextPage();
        return commentReplyExtractor.getPage(nextRepliesPage);
    }

    public CommentReplyExtractor getCommentsExtractor() {
        return this.commentsExtractor;
    }

    public void setCommentsExtractor(CommentReplyExtractor commentsExtractor) {
        this.commentsExtractor = commentsExtractor;
    }

}
