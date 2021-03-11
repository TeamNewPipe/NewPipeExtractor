package org.schabi.newpipe.extractor.comments;

import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import java.util.List;

import javax.annotation.Nonnull;

public abstract class CommentReplyExtractor extends CommentsExtractor {

    public CommentReplyExtractor(final StreamingService service,
                                 final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    public abstract Page getNextPage();

    public abstract void extractReplies() throws ParsingException;

    public abstract List<CommentsInfoItem> getReplies() throws ParsingException;

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return "Replies";
    }
}
