package org.schabi.newpipe.extractor.bulletComments;

import javax.annotation.Nonnull;

import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import java.io.IOException;
import java.util.List;

public abstract class BulletCommentsExtractor extends ListExtractor<BulletCommentsInfoItem> {
    public BulletCommentsExtractor(final StreamingService service,
                                   final ListLinkHandler uiHandler) {
        super(service, uiHandler);
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return "BulletComments";
    }

    @Override
    public InfoItemsPage<BulletCommentsInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        return null;
    }

    public List<BulletCommentsInfoItem> getLiveMessages() throws ParsingException {
        return null;
    }

    public boolean isLive() {
        return false;
    }

    public boolean isDisabled() {
        return false;
    }

    public void disconnect() {

    }

    public void reconnect() {

    }

    public void setCurrentPlayPosition(final long currentPlayPosition) {
    }

    public void clearMappingState() {
    }
}
