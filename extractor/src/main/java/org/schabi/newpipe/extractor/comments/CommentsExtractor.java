package org.schabi.newpipe.extractor.comments;

import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import javax.annotation.Nonnull;

public abstract class CommentsExtractor extends ListExtractor<CommentsInfoItem> {

    public CommentsExtractor(StreamingService service, ListLinkHandler uiHandler) {
        super(service, uiHandler);
        // TODO Auto-generated constructor stub
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return "Comments";
    }
}
