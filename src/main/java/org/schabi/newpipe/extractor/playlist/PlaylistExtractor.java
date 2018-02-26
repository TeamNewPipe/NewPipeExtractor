package org.schabi.newpipe.extractor.playlist;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

public abstract class PlaylistExtractor extends ListExtractor {

    public PlaylistExtractor(StreamingService service, String url) throws ExtractionException {
        super(service, url);
    }

    @Nonnull
    @Override
    protected UrlIdHandler getUrlIdHandler() {
        return getService().getPlaylistUrlIdHandler();
    }

    @NonNull
    @Override
    public InfoItemsCollector getInfoItems()
        throws IOException, ExtractionException {
        return getStreams();
    }

    public abstract StreamInfoItemsCollector getStreams() throws IOException, ExtractionException;
    public abstract String getThumbnailUrl() throws ParsingException;
    public abstract String getBannerUrl() throws ParsingException;

    public abstract String getUploaderUrl() throws ParsingException;
    public abstract String getUploaderName() throws ParsingException;
    public abstract String getUploaderAvatarUrl() throws ParsingException;

    public abstract long getStreamCount() throws ParsingException;
}
